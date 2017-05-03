
import React from 'react';
import TextInput from 'grommet/components/TextInput';

export class ChatInputBox extends React.Component {

    render() {
        return (
            <form onSubmit={(e) => {e.preventDefault(); this.props.submitMessage(); return false;}}>
                <TextInput placeHolder="Your Message Here"
                           value={this.props.reflectInputOnUI}
                           onDOMChange={this.props.handleChatMessageInput}
                           style={{width: 100 + '%'}}/>
            </form>
        )
    }
}