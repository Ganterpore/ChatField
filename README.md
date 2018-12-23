# ChatField

Chatfield is made to be a portable chat app that can be reused in other applications. It stands as a chat app in itself, but is designed so that parts of it can be copied out and used in future projects that require chat functionality. The following will describe the architecture of the app, and how it can be used in other applications.

## Architecture

### L0: Database
This is where the actual data is stored for the app. It uses a NoSQL Firebase Firestore database to store the chats, users, images etc. As it is built on the fly, a lot of this structure is built from the higher levels of the architecture and will not have actual code in here.

Using just the code from this layer will allow the creation of a non-android app to interact with the chat structure. This is currently not very portable however, as the structure is mainly built from higher levels.

#### Database Design
insert image showing the noSQL design of the DB

#### Database Functions
link to the functions, and descriptions of them

### L1: Database Interface
This layer contains classes which directly interact with the database and server, and therefore controls the available interactions. There are the visible classes written for this project, however this layer also contains the imported firebase layers. In general the higher level user layers should not interact with this layer directly, and instead go through Controllers.

### L2: Controllers
The Controllers layer describes the way the application should and does interact with the database. All interactions with the database by the application should go through this layer, and this will control what types of interactions are possible. This is everythng from sending messages, to creating new accounts. This layer also describes how the application interacts directly with the phone, for things like accessing the camera and other basic features.

By copying this and the lower layers, a new app could be created with a different user interface, that has the same chatting and account functionality.

### L3: 

### L4:

### Models
