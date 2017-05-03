
import {JOIN_ACCEPTED} from "./UserSessionReducer.js";

export const USER_LEFT = 'USER_LEFT';
export const NEW_USER_JOINED = 'NEW_USER_JOINED';
export const LOGIN_NAME_TAKEN = "LOGIN_NAME_TAKEN";

export const userLeftChat = (name) => {
    return {
        type: USER_LEFT,
        name
    }
};

export const newUserJoined = (name) => {
    return {
        type: NEW_USER_JOINED,
        name
    }
};

export const nameAlreadyTaken = () => {
  return {
      type: LOGIN_NAME_TAKEN
  }
};

const userLoginReducer = (state = [], action) => {

    switch (action.type) {

        case JOIN_ACCEPTED:
            return action.loggedInUsers;

        case USER_LEFT:
            return state.filter(user => user.name !== action.name);

        case LOGIN_NAME_TAKEN:
            alert("Someone with that name is already present in the chat.  Please pick another name.");
            return state.slice();

        default:
            return state.slice();
    }
};

export default userLoginReducer;