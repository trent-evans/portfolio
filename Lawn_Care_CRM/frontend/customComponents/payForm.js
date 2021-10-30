import React from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import axios from 'axios';
import {Accordion, Button, Card, Col, Form} from "react-bootstrap";

class PayForm extends React.Component{

    constructor(props) {
        super(props);
        this.state = {
            cus_id: this.props.cus_id,
            cus_name: this.props.cus_name,
            due: 0,
            pay_amount: 0,
            pay_type: "no_entry",
            check_num: 0
        }
    }

    componentDidMount() {
        this.getPayDetail()
    }

    getPayDetail(){
        let payDetailURL = 'http://localhost:8080/customer_payment/' + this.state.cus_id
        axios.get(payDetailURL)
            .then(res => {
                this.setState({due:res.data.due})
            })
            .catch(err => (
                console.log(err)
            ))
    }

    async submitPayment() {
        let checkNum, pay_type, pay_amount

        if(this.state.pay_amount){
            pay_amount = this.state.pay_amount
        }else{
            // Need a popup or something here to indicate that fields must be entered
            console.log("Need a payment amount - won't submit payment")
            return
        }

        // If nothing is selected, state doesn't update. Default is check, so set that
        if(this.state.pay_type === "no_entry"){
            pay_type = "Check"
        }else{
            pay_type = this.state.pay_type
        }

        if(pay_type === "Cash"){
            checkNum = "none"
        }else if (pay_type === "Check" && !this.state.check_num){ // If we have a check but no check number
            console.log("Need to enter a check number")
            return
        }else{
            checkNum = this.state.check_num
        }

        const post = {cus_id: this.state.cus_id,
                    pay_amount: pay_amount,
                    pay_type: pay_type,
                    check_num: checkNum};

        await axios.post('http://localhost:8080/postPayment',post)
            .then(res => {
                console.log("Response data: ",res.data);
                if (res.data.response) { // Keep going
                    console.log("Payment recorded")

                }else{ // Error recording payment
                    console.log("Payment did not record")

                }
            })
        this.getPayDetail()
    }

    trackPayType(e){
        this.setState({pay_type:e.target.value});
    }

    trackPayAmount(e){
        this.setState({pay_amount:e.target.value});
    }

    trackCheckNum(e){
        this.setState({check_num:e.target.value});
    }


    render() {
        if(!this.state.due || this.state.due === 0){
            return(
                <Card>
                    <Accordion.Toggle eventKey={this.state.cus_id} as={Card.Header} onClick={event => this.getPayDetail()}>
                        {this.state.cus_name}
                    </Accordion.Toggle>
                    <Accordion.Collapse eventKey={this.state.cus_id}>
                        <Card.Body>
                            <Card.Title>Current Outstanding: $0</Card.Title>
                        </Card.Body>
                    </Accordion.Collapse>
                </Card>
            )
        }

        return(
            <Card>
                <Accordion.Toggle eventKey={this.state.cus_id} as={Card.Header} onClick={event => this.getPayDetail()}>
                    {this.state.cus_name}
                </Accordion.Toggle>
                <Accordion.Collapse eventKey={this.state.cus_id}>
                    <Card.Body>
                        <Card.Title>Current Outstanding ($):</Card.Title>
                        <Card.Text>{this.state.due}</Card.Text>
                        <Card.Title>Add Payment</Card.Title>
                            <Form>
                                <Form.Row>
                                    <Form.Group as={Col}>
                                        <Form.Label>Payment Type*</Form.Label>
                                        <Form.Control as="select" name={"pay_type"} onChange={this.trackPayType.bind(this)}>
                                            <option>Check</option>
                                            <option>Cash</option>
                                        </Form.Control>
                                    </Form.Group>
                                    <Form.Group as={Col}>
                                        <Form.Label>Check #</Form.Label>
                                        <Form.Control placeholder={"Check Number"} onChange={this.trackCheckNum.bind(this)}/>
                                    </Form.Group>
                                    <Form.Group as={Col}>
                                        <Form.Label>Ammount ($)*</Form.Label>
                                        <Form.Control placeholder={"$ (USD)"} onChange={this.trackPayAmount.bind(this)}/>
                                    </Form.Group>
                                </Form.Row>
                                <Button onClick={e => this.submitPayment()}>Record Payment</Button>
                            </Form>
                    </Card.Body>
                </Accordion.Collapse>
            </Card>
        )
    }
}

export default PayForm;