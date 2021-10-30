import React from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import './paymentList.css'
import axios from 'axios';
import {Accordion, Button, Card, Col, Form} from "react-bootstrap";
import PayForm from './payForm';

class PaymentList extends React.Component{

    constructor(props) {
        super(props);
        this.state = {
            landID: this.props.land_id,
            paymentDetail: []
        }
    }

    getPaymentDetails(){
        let paymentURL = 'http://localhost:8080/getOutstandingPayments/' + this.state.landID
        axios.get(paymentURL)
            .then(res => {
                this.setState({paymentDetail:res.data})
            })
            .catch(err => {
                console.log("Error retreiving payment data - getPaymentsDue")
                console.log(err)
            })
    }

    componentDidMount() {
        this.getPaymentDetails()
    }

    render() {
        if (this.state.paymentDetail){
            return(
            <Accordion className={'payment-accordion'}>
                {this.state.paymentDetail.map(customerPay => (
                    <PayForm cus_id={customerPay.cus_id} cus_name={customerPay.name}/>
                ))}
            </Accordion>
            )
        }

        // If the data hasn't been pulled yet
        return(
            <Accordion className={'payment-accordion'}>
            <Card>
                <Accordion.Toggle eventKey={0} as={Card.Header}>
                    Loading data still
                </Accordion.Toggle>
                <Accordion.Collapse eventKey={0}>
                    <Card.Title>Still loading...</Card.Title>
                </Accordion.Collapse>
            </Card>
        </Accordion>
        )
    }
}

export default PaymentList;