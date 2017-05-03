
import React from 'react';
import { connect } from 'react-redux';
import { ChatInputBox } from '../../components/chatbox/ChatInputBox.js';
import { ChatMessages } from '../../components/chatbox/ChatMessages.js';
import { LoggedInUsers } from '../../components/chatbox/LoggedInUsers.js';
import { postNewChatMessage } from "../../reducers/ChatMessageReducer";

@connect(
    (store) => {
        return {
            loggedInUsers: store.loggedInUsers,
            messagesInChat: store.messagesInChat
        }
    }
)

class ChatBoxContainer extends React.Component {

    constructor(props) {
        super(props);
        this.handleChatMessageInput = this.handleChatMessageInput.bind(this);
        this.submitMessage = this.submitMessage.bind(this);
        this.scrollToBottomOfChat = this.scrollToBottomOfChat.bind(this);
        this.state = {
            chatBoxInputValue: ""
        };
    };

    componentDidUpdate() {
        this.scrollToBottomOfChat();
    }

    scrollToBottomOfChat() {
        this.chatDiv.scrollTop = this.chatDiv.scrollHeight
    };

    submitMessage() {
        this.props.dispatch(postNewChatMessage(this.state.chatBoxInputValue));
        this.setState({chatBoxInputValue: ""});
    };

    handleChatMessageInput(event) {
        this.setState({
            chatBoxInputValue: event.target.value
        });
    };

    render() {

        return (
            <div>
                <div className="row" style={{minHeight: 21+"em", maxHeight: 21+"em"}}>
                    <div id="ChatBox" className="col-sm-12"
                         style={{maxHeight: "20em", minHeight: "20em", "overflow-y": "scroll"}}
                         ref={ (input) => { this.chatDiv = input; }}>
                        <ChatMessages messagesInChat={this.props.messagesInChat}/>
                    </div>
                </div>
                <div className="col-sm-12">
                    <ChatInputBox reflectInputOnUI={this.state.chatBoxInputValue}
                                  submitMessage={this.submitMessage}
                                  handleChatMessageInput={this.handleChatMessageInput}/>

                    <LoggedInUsers loggedInUsers={this.props.loggedInUsers}/>
                </div>
            </div>
        )
    };
}

export default ChatBoxContainer;