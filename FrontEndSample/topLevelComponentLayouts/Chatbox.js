
import React from 'react';
import { connect } from 'react-redux';
import { Provider } from 'react-redux'
import { combineReducers, createStore } from "redux";
import CircularProgress from 'material-ui/CircularProgress';
import ChatBoxContainer from "../containers/chatBox/ChatBoxContainer.js"
import Heading from 'grommet/components/Heading';
import 'grommet/scss/vanilla/index.scss';
import Animate from 'grommet/components/Animate';
import Paragraph from 'grommet/components/Paragraph';
import ChatIcon from 'grommet/components/icons/base/Chat';
import TextInput from 'grommet/components/TextInput';
import FormField from 'grommet/components/FormField';
import { BottomNavigation, BottomNavigationItem } from 'material-ui/BottomNavigation';
import {JOIN_ACCEPTED, JOIN_CHAT, NOT_JOINED, joinChat} from "../reducers/UserSessionReducer";

@connect(
    (store) => {
        return {sessionLoginStatus: store.userSession.sessionLoginStatus}
    }
)

export default class Chatbox extends React.Component {

    constructor(props) {
        super(props);
        this.attemptLogin = this.attemptLogin.bind(this);
        this.attemptingALogin = this.attemptingALogin.bind(this);
    }

    attemptLogin(name) {
        this.props.dispatch(joinChat(name))
    }

    attemptingALogin() {
        return this.props.sessionLoginStatus === JOIN_CHAT
    }

    onLoginScreen() {
        return this.attemptingALogin() || this.props.sessionLoginStatus === NOT_JOINED
    }

    wrap(inner) {
        return (
            <div style={this.onLoginScreen() ? {} : {width: 100+"%"}}>

                { inner }

                <Animate enter={{"animation": "fade", "duration": 1000, "delay": 300}}
                         visible={this.attemptingALogin()} keep={true}>
                    <CircularProgress size={60} thickness={7} style={{position: 'absolute', top: 47 + '%', left: 47 + '%'}}/>
                </Animate>
            </div>
        )
    }

    render() {

        switch (this.props.sessionLoginStatus) {

            case NOT_JOINED:
                return this.wrap(
                    (<LoginScreen attemptLogin={this.attemptLogin} visible={true} tryingToLogIn={this.onLoginScreen()}/>)
                );

            case JOIN_CHAT:
                return this.wrap(
                    (<LoginScreen attemptLogin={this.attemptLogin} tryingToLogIn={this.onLoginScreen()} visible={false}/>)
                );

            case JOIN_ACCEPTED:
                return this.wrap(
                    (<ChatScreen tryingToLogIn={this.attemptingALogin()}/>)
                );

            default:
                return this.wrap(
                    (<LoginScreen handleLoginName={this.attemptLogin} visible={true} tryingToLogIn={this.onLoginScreen()}/>)
                );
        }
    }
}

class LoginScreen extends React.Component {

    constructor(props) {
        super(props);
        this.state = {loginName: ""};
        this.handleLoginNameInput = this.handleLoginNameInput.bind(this);
    }

    handleLoginNameInput(event) {

        let maxLengthOfLoginName = 8;
        if(event.target.value.length > maxLengthOfLoginName) {
            let input = event.target.value.slice(0, maxLengthOfLoginName);
            this.setState({loginName: input});
        } else {
            this.setState({loginName: event.target.value});
        }
    }

    render() {

        let showWhenUserLoggedOut = this.props.visible;

        return (
            <div style={{minHeight: 22+"em", maxHeight: 25+"em", "padding": "1em"}}>
            <Animate enter={{"animation": "fade", "duration": 1000, "delay": 300}} visible={showWhenUserLoggedOut} keep={true}>
                <Heading strong={false} uppercase={true} tag='h6' align='center'>
                    An End-to-End Reactive Chat <br/><br/> <ChatIcon/>
                </Heading>
                <Animate enter={{"animation": "fade", "duration": 1000, "delay": 1000}}>
                    <Paragraph size="medium" style={{"margin": "auto", marginBottom: "2em", textAlign: "center"}}>
                        Made with a combination of React-Redux, Scala's Play-Framework, and Grommet UI
                    </Paragraph>
                    <Animate enter={{"animation": "fade", "duration": 1000, "delay": 2000}}>
                        <div style={{margin: 'auto', width: 50 + '%'}}>
                            <form onSubmit={(e) => {e.preventDefault(); this.props.attemptLogin(this.state.loginName); return false;}}>
                                <FormField label='Username' style={{textAlign: "center"}}>
                                    <TextInput required onDOMChange={this.handleLoginNameInput} value={this.state.loginName} style={{textAlign: "center"}}/>
                                </FormField>
                            </form>
                        </div>
                    </Animate>
                </Animate>
            </Animate>
            </div>
        )
    }
}

class ChatScreen extends React.Component {

    render() {
        return (
            <div>
                <Animate enter={{"animation": "fade", "duration": 1000, "delay": 300}} visible={true} keep={true}>
                    <div className="col-sm-1"/>
                    <div className="col-sm-12">
                        <ChatBoxContainer/>
                    </div>
                    <div className="col-sm-2"/>
                </Animate>
            </div>
        )
    }
}