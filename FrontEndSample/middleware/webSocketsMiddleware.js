
import {JOIN_ACCEPTED, populateChatAfterJoin, JOIN_CHAT, joinChat, JOINING} from "../reducers/UserSessionReducer";
import {userLeftChat, USER_LEFT, LOGIN_NAME_TAKEN, nameAlreadyTaken} from "../reducers/UserLoginReducer";
import {newChatMessage, NEW_CHAT_MESSAGE, POST_NEW_CHAT_MESSAGE, postNewChatMessage} from "../reducers/ChatMessageReducer";

let backendIP = document.getElementById("appRoot").getAttribute("data-name");
let socket = new WebSocket("ws://" + backendIP + ":9000/socket");

const webSocketsMiddleware = (store) => (next) => (action) => {

    socket.onmessage = response => {

        let data = JSON.parse(response.data);

        switch(data.type) {

            case JOIN_ACCEPTED:
                store.dispatch(populateChatAfterJoin(data.loggedInUsers));
                break;

            case USER_LEFT:
                store.dispatch(userLeftChat(data.name));
                break;

            case LOGIN_NAME_TAKEN:
                store.dispatch(nameAlreadyTaken());
                break;

            case NEW_CHAT_MESSAGE:
                store.dispatch(newChatMessage(data.author, data.time, data.content));
                break;
        }
    };

    function sleep (time) {
        return new Promise((resolve) => setTimeout(resolve, time));
    }

    switch(action.type) {

        case JOIN_CHAT:
            store.dispatch({type: JOINING});

            sleep(2000).then(() => {
                socket.send(JSON.stringify(joinChat(action.name)));
                next(action);
            });

            break;

        case POST_NEW_CHAT_MESSAGE:
            socket.send(JSON.stringify(postNewChatMessage(action.content)))
            break;

    }

    next(action);
};

export default webSocketsMiddleware