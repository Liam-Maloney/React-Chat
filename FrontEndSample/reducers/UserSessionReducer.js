
import {LOGIN_NAME_TAKEN} from "./UserLoginReducer";

export const JOIN_CHAT = 'JOIN_CHAT';
export const NOT_JOINED = 'NOT_JOINED';
export const JOIN_ACCEPTED = 'JOIN_ACCEPTED';
export const JOINING = 'JOINING';

export const joinChat = (name) => {
    return {
        type: JOIN_CHAT,
        name
    }
};

export const populateChatAfterJoin = (loggedInUsers) => {

    return {
        type: JOIN_ACCEPTED,
        loggedInUsers
    }
};

const userSessionReducer = (state = {sessionLoginStatus: NOT_JOINED}, action) => {

    switch (action.type) {

        case NOT_JOINED:
            return {...state, sessionLoginStatus: NOT_JOINED};

        case LOGIN_NAME_TAKEN:
            return {...state, sessionLoginStatus: NOT_JOINED};

        case JOIN_CHAT:
            return {...state, sessionLoginStatus: JOIN_CHAT};

        case JOINING:
            return {...state, sessionLoginStatus: JOIN_CHAT};

        case JOIN_ACCEPTED:
            return {...state, sessionLoginStatus: JOIN_ACCEPTED};

        default:
            return state
    }
};

export default userSessionReducer;