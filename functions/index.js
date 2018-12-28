const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

exports.addContact = functions.https.onCall((data, context) => {
	//getting user details
	const senderID = context.auth.uid;
 	const senderFirstname = data.firstname;
 	const senderLastname = data.lastname;
 	const senderEmail = data.email;
 	
 	const receiverEmail = data.receiverEmail;
 	
 	//finding user with the contact request emai
 	return admin.firestore().collection('user')
 		.where('email', '==', receiverEmail).get().then(querySnapshot => {
 			querySnapshot.forEach(documentSnapshot => {
 					const receiverID = documentSnapshot.get('userID');
 					console.log('receiverID is ', receiverID);
 					
 					//adding request to recievers requests
 					admin.firestore().doc(`user/${receiverID}/requests/${senderID}`)
 							.set({
 									email: senderEmail,
 									firstname: senderFirstname,
 									lastname: senderLastname, 
 									userID: senderID
 							});
 					
 					//getting the application token for the user from the database
 					return admin.firestore().doc(`tokens/${receiverID}`)
 						.get().then(documentSnapshot => {
 								const token = documentSnapshot.get('token');
 								console.log('notification being sent to ', token);
 								
 								//creating notification to be sent
 								const notif = {
 									notification: {
 										title: 'New Contact Request',
 										body: `${senderFirstname} has added you as a contact`,
 										clickAction: 'OPEN_ACCEPT_CONTACT_ACTIVITY',
 									},
 									data: {
 										userID: senderID,
 										firstname: senderFirstname,
 										lastname: senderLastname,
 										email: senderEmail,
 									},
 								};
 								
 								//sending request notification to given token
 								return admin.messaging().sendToDevice(token, notif);
 						});
 				});
 		});
});

exports.sendNotificationToUser = functions.https.onCall((data, context) => {
		const notif = data.notification;
		const receiverID = data.userID;
		
		return admin.firestore().doc(`tokens/${receiverID}`).get()
			.then(documentSnapshot => {
					const token = documentSnapshot.get('token');
					console.log('notification being sent to ', token);

					//sending notification
					return admin.messaging().sendToDevice(token, notif);
		});
});


exports.acceptContact = functions.https.onCall((data, context) => {
		//getting the passed information
		const uid = context.auth.uid;
		const contactID = data.contactID;
		
		//creating a chat between users
		console.log('creating chat ');
		//const chat = {user1ID: uid, user2ID: contactID, user1Seen: false, user2Seen:false};
		const chat = {
			users: [uid, contactID],
			seen: [false, false]
		}
		const newConv = admin.firestore().collection(`conversations`)
			.add(chat);
			
		return newConv.then(documentReference => {
			const conversationid = documentReference.id;
			console.log('chat ', conversationid, 'created');
		
			console.log('getting request data');
			//finding the contact request assosciated with the contactID from the database
			const contactRequest = admin.firestore().doc(`user/${uid}/requests/${contactID}`);
			return contactRequest.get().then(documentSnapshot => {
				console.log(uid, ' adding contact ', contactID, 
					documentSnapshot.get('firstname'), documentSnapshot.get('lastname'), 
					documentSnapshot.get('email'), conversationid);
				
				//getting contact data from the contact request, and new conversationID
				const contactData = {
					userID: contactID,
					firstname: documentSnapshot.get('firstname'),
					lastname: documentSnapshot.get('lastname'),
					email: documentSnapshot.get('email'),
					conversationID: conversationid
				}
				
				//adding contact to the requested user
				admin.firestore().doc(`user/${uid}/contacts/${contactID}`)
					.set(contactData).then(documentSnapshot => {
						//removing the contact request
						console.log('removing request ', contactID);
						contactRequest.delete();
				});	
					
				//adding the new conversation to both users
				const conversationData = {
					name: documentSnapshot.get('firstname') + " " + documentSnapshot.get('lastname'),
					lastMessageSentAt: Date.now(),
					conversationID: conversationid,
					type: "contact",
					seen:false
				}
				admin.firestore().doc(`user/${uid}/conversations/${conversationid}`)
					.set(conversationData);
				
				console.log('getting contact user');
				return admin.firestore().doc(`user/${uid}`).get().then(documentSnapshot => {
					console.log('contact being added on requester side');
					const userFirstname = documentSnapshot.get('firstname');
					//getting the requested user data
					const userData = {
						userID: uid,
						firstname: userFirstname,
						lastname: documentSnapshot.get('lastname'),
						email: documentSnapshot.get('email'),
						conversationID: conversationid
					}
					
					const otherConversationData = {
						name: documentSnapshot.get('firstname') + " " + documentSnapshot.get('lastname'),
						lastMessageSentAt: Date.now(),
						conversationID: conversationid,
						type: "contact",
						seen:false
					}
					
					admin.firestore().doc(`user/${contactID}/conversations/${conversationid}`)
					.set(otherConversationData);
					
					//getting application token assosciated with the requester
					admin.firestore().doc(`tokens/${contactID}`).get()
						.then(documentSnapshot => {
 								const token = documentSnapshot.get('token');
 								console.log('notification being sent to ', token);
 								//creating request acceptance notificaton
 								const notif = {
 									notification: {
 										title: 'Contact Request Accepted',
 										body: `${userFirstname} has accepted your request`,
 										clickAction: 'OPEN_CONTACT_ACTIVITY',
 									}
 								};
 								//sending notification
 								return admin.messaging().sendToDevice(token, notif);
 						});
					//adding the contact on the requesters side
					return admin.firestore().doc(`user/${contactID}/contacts/${uid}`)
						.set(userData);	
					});
				});
		});
});

exports.createGroupChat = functions.https.onCall((data, context) => {
	//initialising variables
	const name = data.name;
	const memberIds = data.users;
	const nMembers = data.nMembers;
	
	var seen = [];
	for(var i=0;i<nMembers;i++) {
		seen[i] = false;
	}
	
	//building chat on database
	const chatOnline = {
		name:name,
		users:memberIds,
		seen:seen
	}
	return admin.firestore().collection(`conversations`)
		.add(chatOnline).then(documentReference => {
			const conversationid = documentReference.id;
			console.log('chat ', conversationid, 'created');
			
			//adding chat locally
			const chatLocal = {
				conversationID: conversationid,
				lastMessageSentAt: Date.now(),
				type: "group",
				seen: false,
				name:name
			}
			
			for(var i=0;i<memberIds.length;i++) {
				const memberId = memberIds[i];
				admin.firestore().doc(`user/${memberId}/conversations/${conversationid}`)
						.set(chatLocal);
			}
		});
});

exports.messageNotification = functions.firestore
	.document(`conversations/{conversationID}/message/{messageID}`)
	.onCreate((snapshot, context) => {
		const messageDoc = snapshot.data();
		const conversationID = context.params.conversationID;
		console.log('recieved message: ', messageDoc.message);
	
		return admin.firestore().doc(`conversations/${conversationID}`).get()
			.then(documentSnapshot => {
				conversationDoc = documentSnapshot.data();
				const nUsers = conversationDoc.users.length;
				const senderIndex = conversationDoc.users.indexOf(messageDoc.userID);
				var seenArray = new Array(nUsers);
				
				const notif = {
					notification: {
						title: "Message received",
						body: messageDoc.message,
						clickAction: 'OPEN_MESSAGE_ACTIVITY'
					},
					data: {
						conversationID: conversationID
					}
				}
				
				//set all to unseen, except the sender
				var i;
				for(i=0;i<nUsers;i++) {
					if(i == senderIndex) {
						seenArray[i] = true;
					} else {
						seenArray[i] = false;
						
						//sending notification to user
						const receiverID = conversationDoc.users[i]
						admin.firestore().doc(`tokens/${receiverID}`).get()
							.then(documentSnapshot => {
								const token = documentSnapshot.get('token');
								console.log('notification being sent to ', token);

								//sending notification
								admin.messaging().sendToDevice(token, notif);
						});
					}
				}
				
				admin.firestore().doc(`conversations/${conversationID}`).set({
							seen: seenArray
						}, {merge: true});
			});
});