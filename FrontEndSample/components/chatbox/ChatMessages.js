import React from 'react';
import {List, ListItem} from 'material-ui/List';
import {grey400, darkBlack, lightBlack} from 'material-ui/styles/colors';
import Table from 'grommet/components/Table';
import TableRow from 'grommet/components/TableRow';
import Chip from 'material-ui/Chip';


export class ChatMessages extends React.Component {

    render() {

        let messagesInChat = this.props.messagesInChat;

        return (
            <Table>
                <tbody>
                {messagesInChat.map( message =>
                    <div className="row">
                        <div key={message.key}>
                            <TableRow className="col-sm-12">
                                <div className="col-md-2" style={{}}>
                                    <td style={{}}>
                                        <Chip style={{"position": "relative", right: 2+"em"}}>
                                            {message.author}
                                        </Chip>
                                    </td>
                                </div>
                                <div className="col-md-10">
                                    <td className='secondary'>
                                        {message.content}
                                        <br/> {message.time}
                                    </td>
                                </div>
                            </TableRow>
                        </div>
                    </div>
                )}
                </tbody>
            </Table>
        )
    }
}