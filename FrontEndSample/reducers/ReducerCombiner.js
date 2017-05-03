
import { combineReducers } from 'redux';
import chatMessagesReducer from './ChatMessageReducer.js'
import userLoginReducer from './UserLoginReducer.js'
import userSessionReducer from './UserSessionReducer.js'

const reducers = combineReducers({
    loggedInUsers: userLoginReducer,
    messagesInChat: chatMessagesReducer,
    userSession: userSessionReducer
});

export default reducers;