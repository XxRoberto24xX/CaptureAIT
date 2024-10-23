from flask import Flask, request, session
from flask_socketio import join_room, leave_room, emit, SocketIO
import random
import threading
from string import ascii_uppercase
import base64
from PIL import Image
from io import BytesIO
from ultralytics import YOLO
import time
import pydoc

app = Flask(__name__)
app.config["SECRET_KEY"] = "dlk238adfni2389r8ha21"
socketio = SocketIO(app)

#VARIABLES
#trained model do detect elements in photos
modelo = YOLO("yolov8m.pt")

#models needed for the game function
rooms = {}  #contains all the rooms and information needed of them as a dictionary
personal_rooms = [] #contains one room for each person connected to socket io 
mailBox = {}   #contains all the messages a user have
deletedFriends = {} #contains all the friends who deleted you when you where not connected
newFriends = {} #contains all the friends who accepted you request while you where not connected

#this dictionary has all the elements the model can detect
diccionario = {
    0: "persona", 1: "bicicleta", 2: "coche", 3: "motocicleta", 4: "avión", 5: "autobús", 6: "tren", 
    7: "camión", 8: "barco", 9: "semáforo", 10: "hidrante", 11: "señal de alto", 12: "parquímetro", 
    13: "banco", 14: "pájaro", 15: "gato", 16: "perro", 17: "caballo", 18: "oveja", 19: "vaca", 
    20: "elefante", 21: "oso", 22: "cebra", 23: "jirafa", 24: "mochila", 25: "paraguas", 26: "bolso de mano", 
    27: "corbata", 28: "maleta", 29: "frisbee", 30: "esquís", 31: "tabla de snowboard", 32: "pelota deportiva", 
    33: "cometa", 34: "bate de béisbol", 35: "guante de béisbol", 36: "monopatín", 37: "tabla de surf", 
    38: "raqueta de tenis", 39: "botella", 40: "copa de vino", 41: "taza", 42: "tenedor", 43: "cuchillo", 
    44: "cuchara", 45: "tazón", 46: "plátano", 47: "manzana", 48: "sándwich", 49: "naranja", 50: "brócoli", 
    51: "zanahoria", 52: "perrito caliente", 53: "pizza", 54: "dona", 55: "pastel", 56: "silla", 57: "sofá", 
    58: "planta en maceta", 59: "cama", 60: "mesa de comedor", 61: "inodoro", 62: "televisión", 63: "portátil", 
    64: "ratón", 65: "control remoto", 66: "teclado", 67: "teléfono móvil", 68: "microondas", 69: "horno", 
    70: "tostadora", 71: "fregadero", 72: "refrigerador", 73: "libro", 74: "reloj", 75: "jarrón", 
    76: "tijeras", 77: "oso de peluche", 78: "secador de pelo", 79: "cepillo de dientes"
}




### DETECCTION SERVICE ###

@app.route('/')
def index():
    """Home route that returns a simple message indicating the server is running."""
    return "hola estoy funcionando"

# EVALUATION receives the photo made by the player on their mobile phone and detects the elements in it
@app.route("/evaluate", methods=["POST"])
def evaluate():
    """
    Endpoint to evaluate an image sent by the user.

    This function decodes the received image, processes it using a trained YOLO model,
    and updates the game state with detected elements.
    
    Returns:
        dict: A dictionary containing detected elements and their positions.
    """

    # Retrieve the image and user details from the POST request
    imgEnc = request.form["image"]
    nameUser = request.form["name"]
    code = request.form["code"]

    # Decode the image
    img = base64.b64decode(imgEnc)
    img = Image.open(BytesIO(img))

    # Rescale the image for faster processing
    img = img.resize((360, 640))
    results = modelo.predict(img)

    with rooms[code]["lock"]:
        # Initialize lists to store detected elements and their positions
        detection = []
        position = []  # Position in which the user found the element (first person to find it, second person, ...)
        contador = 0

        # Iterate through detected elements
        for element in results[0].boxes.cls:
            if results[0].boxes.conf[contador] > 0.7:
                print(diccionario[int(element)])
                if diccionario[int(element)] in rooms[code]["detection"]:
                    userIndex = rooms[code]["names"].index(nameUser)
                    index = rooms[code]["detection"].index(diccionario[int(element)])
                    if not rooms[code]["state"][userIndex][index]:
                        rooms[code]["state"][userIndex][index] = True
                        rooms[code]["num_detections"][index] += 1
                        detection.append(diccionario[int(element)])
                        position.append(rooms[code]["num_detections"][index])
                        contador += 1
                else:
                    contador += 1
            else:
                contador += 1

        # Return detected elements and their positions
        if len(detection) == 0:
            return {"elements": ["Nada"], "positions": position}
        else:
            return {"elements": detection, "positions": position}




### USER CONNECTION MANAGEMENT ###

# Emitted by the client when they connect to the Socket.IO socket
@socketio.on("connect")
def connect():
    """Handles client connection to the Socket.IO socket."""
    print("Conectado")

# Emitted by the client when they disconnect from the Socket.IO socket
@socketio.on("disconnect")
def disconnect():
    """Handles client disconnection from the Socket.IO socket."""
    leave_server(session.get("name"))
    print(session.get("name"))
    print("Desconectado")

# Emitted by the client when the player has been successfully authenticated and needs to join their rooms
@socketio.on("join_server")
def join_server(userName):
    """
    Handles client request to join the server and relevant rooms.
    
    Args:
        userName (str): The name of the user joining the server.
    """
    # Save the name in cache
    session["name"] = userName

    # Prepare the lists to pass to the client
    codes, names, points, finishes, detections = [], [], [], [], []

    # Add the information to the lists
    for room in rooms:
        if userName in rooms[room]["names"]:
            with rooms[room]["lock"]:
                codes.append(room)
                names.append(rooms[room]["names"])
                points.append(rooms[room]["points"])
                finishes.append(rooms[room]["finish"])
                detections.append(rooms[room]["detection"])

    # Generate empty lists for friends and mailbox if the user has no changes
    if userName not in deletedFriends:
        deletedFriends[userName] = []
    
    if userName not in newFriends:
        newFriends[userName] = []

    if userName not in mailBox:
        mailBox[userName] = []

    # Join a personal room
    new_personal_room(userName)

    # Join the game rooms
    for code in codes:
        join_room(code)

    # Acknowledge with the user information requested
    emit("join_server", {
        "codes": codes, "names": names, "points": points, "finishes": finishes, 
        "detections": detections, "delFr": deletedFriends[userName], 
        "addFr": newFriends[userName], "mailBox": mailBox[userName]
    })

    # Empty the newFriends and deletedFriends lists
    newFriends[userName] = []
    deletedFriends[userName] = []

# Emitted by the client when they are about to disconnect
@socketio.on("leave_server")
def leave_server(name):
    """
    Handles client request to leave the server and all rooms.
    
    Args:
        name (str): The name of the user leaving the server.
    """
    # First leave from the personal room
    leave_personal_room(name)

    # Then leave from every room the user is in
    for code in rooms:
        if name in rooms[code]["names"]:
            leave_room(code)

# Emitted when a user deletes their account
@socketio.on("delete_user")
def delete_user(userName, friendsList):
    """
    Handles user account deletion, cleaning up related data.
    
    Args:
        userName (str): The name of the user deleting their account.
    """
    if userName in deletedFriends:
        del deletedFriends[userName]
    
    if userName in newFriends:
        del newFriends[userName]

    if userName in mailBox:
        del mailBox[userName]

    if userName in personal_rooms:
        leave_server(userName)

    for room in rooms:
        if userName in rooms[room]["names"]:
            abandon(room, userName)

    for friend in friendsList:
        delete_friend(userName, friend)

def leave_personal_room(name):
    """
    Handles the user leaving their personal room.
    
    Args:
        name (str): The name of the user leaving their personal room.
    """
    # Check if the user has an active room to delete
    if name not in personal_rooms:
        return
    
    personal_rooms.remove(name)

    # Remove the user from their room
    leave_room(name)

def new_personal_room(name):
    """
    Handles creating a new personal room for the user.
    
    Args:
        name (str): The name of the user for whom a personal room is created.
    """
    # Check if the user has an active room to avoid duplication
    if name in personal_rooms:
        # Add the user to their room
        join_room(name)
        return
    
    # Add the name to the personal_rooms list
    personal_rooms.append(name)

    # Add the user to their room
    join_room(name)




### ROOM CREATION AND JOINING ###
        
# Emitted by a player when they want to create a new game
@socketio.on("create_room")
def create_room(num_players, userName):
    """
    Handles creating a new game room.

    Args:
        num_players (int): Number of players for the game room.
        userName (str): The name of the user creating the room.
    """
    # Create a unique code
    while True:
        code = ""
        for _ in range(4):
            code += random.choice(ascii_uppercase)

        if code not in rooms:
            break
    
    # Add the room to the dictionaries
    rooms[code] = {
        "members": num_players, "finish_received": 0, "started": False, 
        "finish": False, "names": [], "points": [], "detection": [], 
        "num_detections": [], "state": [], "start_time": time.time(), 
        "lock": threading.Lock()
    }

    with rooms[code]["lock"]:
        # Add the information of the creator player
        rooms[code]["names"].append(userName)
        rooms[code]["points"].append(0)
        rooms[code]["state"].append([False, False, False, False])

    # Add the creator to the room
    join_room(code)

    # Inform the client about the success and send the room code to them
    emit("create_room", code)

# Emitted by a player when they want to join a new game
@socketio.on("join_new")
def join_new(code, name):
    """
    Handles a player joining a new game room.

    Args:
        code (str): The code of the room the player wants to join.
        name (str): The name of the player joining the room.
    """

    # Check if the room exists
    if code not in rooms:
        emit("join_new", {"state": "error"})
        return

    with rooms[code]["lock"]:
        # Check if the game has started
        if rooms[code]["started"] == True:
            emit("join_new", {"state": "error"})
            return

        # Add the user to the room
        rooms[code]["names"].append(name)
        rooms[code]["points"].append(0)
        rooms[code]["state"].append([False, False, False, False])

        # Join the user to the room
        join_room(code)

        # Check if this is the last possible player
        if len(rooms[code]["names"]) == rooms[code]["members"]:
            # Generate a new list of detectable objects
            indexes = random.sample(list(range(len(diccionario) - 1)), 4)
            random_elements = [list(diccionario.values())[i] for i in indexes]

            prepared_Elements = ["botella", "portátil", "ratón", "teclado"]  # This list has been prepared to test the app

            # Set the room elements and number of detections
            for element in prepared_Elements:
                rooms[code]["detection"].append(element)
                rooms[code]["num_detections"].append(0)

            # Set the game as started
            rooms[code]["started"] = True

            # Inform the user the join succeeded and provide information about it
            emit("join_new", {"state": "ready", "names": rooms[code]["names"], "detections": prepared_Elements})

            # Inform the players who are waiting
            for userName in rooms[code]["names"]:
                if userName != name:
                    emit("ready", {"code": code, "names": rooms[code]["names"], "detections": prepared_Elements}, to=userName)
        else:
            # Inform the user the join succeeded but they need to wait for the whole room to be prepared
            emit("join_new", {"state": "wait"})

# Emitted by a player when they leave the room before it starts
@socketio.on("leave_wait")
def leave(code, name):
    """
    Handles a player leaving the room before the game starts.

    Args:
        code (str): The code of the room the player wants to leave.
        name (str): The name of the player leaving the room.
    """
    with rooms[code]["lock"]:
        # Check if the room exists
        if code not in rooms:
            return
        
        if rooms[code]["started"] == True:
            return

        # Remove the user before the game starts
        index = rooms[code]["names"].index(name)
        del rooms[code]["names"][index]
        del rooms[code]["points"][index]

        leave_room(code)

        # Delete the room if it becomes empty
        if len(rooms[code]["names"]) == 0:
            del rooms[code]



### GAME MANAGEMENT ###

# Emitted by the user with the list of elements the model detected and are relevant to the game
@socketio.on("update")
def update_req(code, nameUser, detection):
    """
    Handles the update request when a user detects elements relevant to the game.

    Args:
        code (str): The code of the game room.
        nameUser (str): The name of the user who detected elements.
        detection (list): List of elements detected by the user.
    """
    with rooms[code]["lock"]:
        # Check if the game has been set as finished while the detection arrived
        if rooms[code]["finish"] == True:
            return

        userIndex = rooms[code]["names"].index(nameUser)

        # Calculate the points the user achieved and add them to their count
        for element in detection:
            index = rooms[code]["detection"].index(element)
            rooms[code]["points"][userIndex] += (32 - (8 * (int(rooms[code]["num_detections"][index]) - 1)))  # 32 - 24 - 16 - 8
        
        # Check if every object was found by this player
        contador = 0
        for state in rooms[code]["state"][userIndex]:
            if state == True:
                contador += 1
        
        # Check if the game ended and should be set as finished
        if contador == 4:
            # The game ended
            rooms[code]["finish"] = True

            # Calculate the total seconds the game lasted
            duration = time.time() - rooms[code]["start_time"]

            # Notify the game has finished and the last update
            emit("update", {"code": code, "user": nameUser, "points": rooms[code]["points"][userIndex], "finish": rooms[code]["finish"], "time": duration}, to=code)

        else:
            # Notify the update 
            emit("update", {"code": code, "user": nameUser, "points": rooms[code]["points"][userIndex], "finish": rooms[code]["finish"], "time": 0}, to=code)

# Emitted when a player wants to abandon a game
@socketio.on("abandon")
def abandon(code, name):
    """
    Handles the player abandoning a game.

    Args:
        code (str): The code of the game room.
        name (str): The name of the player abandoning the game.
    """
    with rooms[code]["lock"]:
        # Reduce the number of members in the room
        rooms[code]["members"] -= 1

        # Find the index of the player to remove
        indexUser = rooms[code]["names"].index(name)

        # Remove the data associated with the player
        del rooms[code]["names"][indexUser]
        del rooms[code]["points"][indexUser]
        del rooms[code]["state"][indexUser]

        # Get the player out of the room
        leave_room(code)

        duration = 0

        # Check if there is only one player left and if the game is finished
        if rooms[code]["members"] == 1:
            # Mark the game as finished
            rooms[code]["finish"] = True

            # Calculate the game duration
            duration = time.time() - rooms[code]["start_time"]

        # Notify that the player left
        emit("abandon", {"code": code, "nameLeft": name, "finish": rooms[code]["finish"], "time": duration}, to=code)

# Emitted when a player has seen the results
@socketio.on("ack_finish")
def ack_finish(code, userName):
    """
    Handles acknowledgement when a player has seen the game results.

    Args:
        code (str): The code of the game room.
        userName (str): The name of the user who acknowledged the results.
    """
    with rooms[code]["lock"]:
        # Update the number of finish received
        rooms[code]["finish_received"] += 1

        # Send the user out of the room
        leave_room(code)

        # Check if everyone has seen the game finish and delete the room if this has happened
        if rooms[code]["finish_received"] == rooms[code]["members"]:
            del rooms[code]




## NAME CHANGE MANAGEMENT ##

# Emitted when a player changes their name
@socketio.on("name_change")
def name_change(oldName, newName, listFriends):
    """
    Handles the name change request when a player changes their name.

    Args:
        oldName (str): The old name of the player.
        newName (str): The new name of the player.
        listFriends (list): List of friends of the player who need to now his name changed.
    """

    # Change the personal room
    if oldName in personal_rooms:
        leave_personal_room(oldName)
        new_personal_room(newName)

    # Save the name in cache
    session["name"] = newName

    # List to fill with all the players who share rooms with the user who changed the name
    notifyList = []

    # Change the name in all the rooms the player is participating in
    for room in rooms:
        if oldName in rooms[room]["names"]:
            index = rooms[room]["names"].index(oldName)
            rooms[room]["names"][index] = newName

            # Notify the players and add them to the notifyList to avoid sending the event twice if you have several games with them
            for player in rooms[room]["names"]:
                if player in personal_rooms and player not in notifyList:
                    notifyList.append(player)
                    emit("cooplayer_name_changed", {"oldName": oldName, "newName": newName}, to=player)

    # Change the name in the rest of the lists
    if oldName in mailBox:
        mailBox[newName] = mailBox.pop(oldName)

    if oldName in deletedFriends:
        deletedFriends[newName] = deletedFriends.pop(oldName)

    if oldName in newFriends:
        newFriends[newName] = newFriends.pop(oldName)

    # Inform the friends
    for friend in listFriends:
        if friend in personal_rooms:
            # If the friend is connected, inform them immediately
            emit("friend_name_changed", {"oldName": oldName, "newName": newName}, to=friend)
        else:
            # If the friend is not connected, tell them to remove the old name when they connect and add the new one
            deletedFriends[friend].append(oldName)
            newFriends[friend].append(newName)





## FRIENDS AND MESSAGES ##

# Emitted when a user wants to delete a message from their mailbox
@socketio.on("delete_message")
def delete_message(type, name, code, myname):
    """
    Handles the deletion of a message from the user's mailbox.

    Args:
        type (str): The type of the message.
        name (str): The name associated with the message.
        code (str): The code associated with the message.
        myname (str): The name of the user requesting the deletion.
    """
    mailBox[myname] = [elemento for elemento in mailBox[myname] if elemento != {"type": type, "name": name, "code": code}]

# Emitted when a user wants to send a friend request
@socketio.on("friend_request")
def friend_request(userName, invitedUserName):
    """
    Handles the sending of a friend request from one user to another.

    Args:
        userName (str): The name of the user sending the request.
        invitedUserName (str): The name of the user receiving the request.
    """
    if invitedUserName not in mailBox:
        mailBox[invitedUserName] = []

    # Add the new message to the recipient's mailbox
    mailBox[invitedUserName].append({"type": "msgFr", "name": userName, "code": ""})

    # If the recipient is connected, inform them about the new message
    if invitedUserName in personal_rooms:
        emit("new_message", {"type": "msgFr", "name": userName, "code": ""}, to=invitedUserName)

# Emitted when a user wants to send an invitation to a game
@socketio.on("invite_request")
def invite_request(userName, invitedUserName, code):
    """
    Handles the sending of a game invitation from one user to another.

    Args:
        userName (str): The name of the user sending the invitation.
        invitedUserName (str): The name of the user receiving the invitation.
        code (str): The code of the game being invited to.
    """
    if invitedUserName not in mailBox:
        mailBox[invitedUserName] = []

    # Add the new message to the recipient's mailbox
    mailBox[invitedUserName].append({"type": "msgInv", "name": userName, "code": code})

    # If the recipient is connected, inform them about the new message
    if invitedUserName in personal_rooms:
        emit("new_message", {"type": "msgInv", "name": userName, "code": code}, to=invitedUserName)

# Emitted when a friend accepts another player's friend request
@socketio.on("accepted_friend_request")
def accepted_friend_request(invitedUserName, userName):
    """
    Handles the acceptance of a friend request.

    Args:
        invitedUserName (str): The name of the user accepting the friend request.
        userName (str): The name of the user who sent the original friend request.
    """
    # See if the added user is connected
    if userName in personal_rooms:
        # Notify them immediately
        emit("accepted_friend_request", invitedUserName, to=userName)
    else:
        # See if there is an entry for the user and create it if needed
        if invitedUserName not in newFriends:
            newFriends[invitedUserName] = []

        # Add the userName to the added friends list of invitedUserName
        newFriends[userName].append(invitedUserName)

# Emitted when a user deletes a friend from their list
@socketio.on("delete_friend")
def delete_friend(userName, delUserName):
    """
    Handles the deletion of a friend from the user's friend list.

    Args:
        userName (str): The name of the user deleting the friend.
        delUserName (str): The name of the friend being deleted.
    """
    # See if the deleted user is connected
    if delUserName in personal_rooms:
        # Notify them immediately
        emit("delete_friend", userName, to=delUserName)
    else:
        # See if there is an entry for the user and create it if needed
        if delUserName not in deletedFriends:
            deletedFriends[delUserName] = []
        
        # Add the userName to the deleted friends list of delUserName
        deletedFriends[delUserName].append(userName)

# Here is where the program really starts and opens the server
if __name__ == '__main__':
    """
    Starts the Socket.IO server.
    """
    socketio.run(app, debug=True, host='0.0.0.0', port=8001)

    #used only to generate the documentation
    #pydoc.writedoc('Server')

