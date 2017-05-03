import {JOIN_ACCEPTED} from "./UserSessionReducer.js";

export const NEW_CHAT_MESSAGE = 'NEW_CHAT_MESSAGE';
export const POST_NEW_CHAT_MESSAGE = 'POST_NEW_CHAT_MESSAGE';

export const newChatMessage = (author, time, content) => {
    return {
        type: NEW_CHAT_MESSAGE,
        author,
        time,
        content
    }
};

export const postNewChatMessage = (content) => {
    return {
        type: POST_NEW_CHAT_MESSAGE,
        time: timenow(),
        content
    }
};

const chatMessagesReducer = (state = [], action) => {

    switch (action.type) {

        case NEW_CHAT_MESSAGE:
            let newMessage = {author: action.author, time: action.time, content: action.content};
            return state.concat(newMessage);

        case JOIN_ACCEPTED:
            if(state.length === 0) {
                let welcomeMessage = {
                    author: "Liam",
                    time: "∞",
                    content: "Welcome to the chat!"
                };
                return state.concat(welcomeMessage);
            } else {
              return state;
            }

        default:
            return state
    }
};

function timenow(){

    let now = new Date(),
        ampm = 'am',
        h = now.getHours(),
        m = now.getMinutes(),
        s = now.getSeconds();
    if(h>= 12) {

        if(h>12) h -= 12;
        ampm = 'pm';
    }

    if(m<10) m= '0'+m;
    if(s<10) s= '0'+s;
    return h + ':' + m + ampm;
}

export default chatMessagesReducer;