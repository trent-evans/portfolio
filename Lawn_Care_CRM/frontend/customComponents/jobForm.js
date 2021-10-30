import React from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import axios from 'axios';
import {Button, Card, Form} from "react-bootstrap";

class JobForm extends React.Component{

    constructor(props) {
        super(props);
        this.state = {
            job_id: this.props.job_id,
            date_planned: this.props.date,
            desc: this.props.desc,
            date_form: "",
            usePlan: false,
            completed: this.props.complete
        }
    }

    async postJobComplete(){
        let dateComplete, jobId
        jobId = this.state.job_id

        if(this.state.usePlan){
            dateComplete = this.state.date_planned
        }else{
            dateComplete = this.state.date_form
        }

        const out = {job_id:jobId,date_complete:dateComplete}
        console.log(out)

        await axios.post('http://localhost:8080/completeJob',out)
            .then(res => {
                if (res.data.complete){
                    console.log(res.data)
                    this.setState({completed:true})
                }else{
                    this.setState({completed:false})
                }
            })
            .catch(err => {
                console.log(err)
            });
    }

    trackUsePlan(e){
        if(!this.state.usePlan || this.state.usePlan === false){ // If the box hasn't been touched yet
            this.setState({usePlan:true})
        }else {
            this.setState({usePlan:false})
        }

    }

    trackFormDate(e){
        this.setState({date_form:e.target.value});
    }

    render(){
        if(this.state.completed){
            return(
                <Card.Body>
                    <Card.Title>Job Complete</Card.Title>
                </Card.Body>
            )
        }

        return(
            <Card.Body>
                <Card.Title>{this.state.desc}</Card.Title>
                <Card.Text><b>Date Planned:</b></Card.Text>
                <Card.Text>{this.state.date_planned}</Card.Text>
                <Card.Text><b>Date Complete:</b></Card.Text>
                <Form>
                    <Form.Group>
                        <Form.Check type={"checkbox"} label={"Use planned date"} onChange={this.trackUsePlan.bind(this)}/>
                        <Form.Control placeholder={"MM/DD/YYYY"} disabled={this.state.usePlan} onChange={this.trackFormDate.bind(this)}/>
                    </Form.Group>
                </Form>
                <Button onClick={event => this.postJobComplete()}>Mark Complete</Button>
            </Card.Body>
        )
    }

}

export default JobForm;