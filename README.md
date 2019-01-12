# ChatField
*Note: this is an unfinished work in progress, and is designed to be used in other projects rather than as a standalone app. While it does work as a messaging app, it is still slow, has a poor user interface and has some glitches.*

Chatfield is made to be a portable chat app that can be reused in other applications. It stands as a chat app in itself, but is designed so that parts of it can be copied out and used in future projects that require chat functionality. The following will describe the architecture of the app, and how it can be used in other applications.

## Architecture

### L0: Database
This is where the actual data is stored for the app. It uses a NoSQL Firebase Firestore database to store the chats, users, images etc. As it is built on the fly, a lot of this structure is built from the higher levels of the architecture and will not have actual code in here.

Using just the code from this layer will allow the creation of a non-android app to interact with the chat structure. This is currently not very portable however, as the structure is mainly built from higher levels.

#### Database Design
![Database Design image](/database%20design.png)

#### [Database Functions](functions/index.js)
link to the functions, and descriptions of them

### L1: [Database Interface](app/src/main/java/com/ganterpore/chatfield/L1_Database_Interface)
This layer contains classes which directly interact with the database and server, and therefore controls the available interactions. There are the visible classes written for this project, however this layer also contains the imported firebase layers. In general the higher level user layers should not interact with this layer directly, and instead go through Controllers.

### L2: [Controllers](app/src/main/java/com/ganterpore/chatfield/L2_Controllers)
The Controllers layer describes the way the application should and does interact with the database. All interactions with the database by the application should go through this layer, and this will control what types of interactions are possible. This is everythng from sending messages, to creating new accounts. This layer also describes how the application interacts directly with the phone, for things like accessing the camera and other basic features.

By copying this and the lower layers, a new app could be created with a different user interface, that has the same chatting and account functionality.

### L3: [View Fragments](app/src/main/java/com/ganterpore/chatfield/L3_View_Fragments)
This layer contains UI fragments which are reused throughout the code. For example the chat page, contacts page etc. By copying the elements in this layer and lower, you can add the generic chat elements to your own app, without needing to design the UI yourself, or do the backend work.

### L4: [User Interface](app/src/main/java/com/ganterpore/chatfield/L4_User_Interface)
This the layer for the user interface for this individual application. It contains example uses of how an app can be made using the other elements, and builds a simple chat application.

### [Models](app/src/main/java/com/ganterpore/chatfield/Models)
The Models layer contains all the Data types used by the database and the code, for example conversations, accounts messages etc.

## Set Up Android Code
### Prerequisites
First make sure that both git and Android Studio are installed on your computer.

### Step 1
Open Android studio, Select File -> New -> project from version control -> git, then fill in the url "https://github.com/Ganterpore/ChatField" 

And its as easy as that! from here you should be able to run the code on an android phone or an emulator, and edit the code however you like!

## Set up Firebase Functions
You should already have the code in the file "/functions/index.js" when you cloned the project. However, there is no access to update or change the functions on the firebase.

//firebase functions set up

