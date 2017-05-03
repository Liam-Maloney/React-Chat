
import React from 'react';
import Accordion from 'grommet/components/Accordion';
import AccordionPanel from 'grommet/components/AccordionPanel';
import Paragraph from 'grommet/components/Paragraph';

export class LoggedInUsers extends React.Component {

    render() {

        const activeUsers = this.props.loggedInUsers;

        return (
            <Accordion>
                <AccordionPanel heading='Active Users'>
                    {activeUsers.map( loggedInUser => <Paragraph>{loggedInUser.name}</Paragraph>)}
                </AccordionPanel>
            </Accordion>
        )
    };
}
