import React, {Component} from 'react';
import axios from 'axios';
import 'bootstrap/dist/css/bootstrap.min.css';
import './detailcards.css';
import {Card} from 'react-bootstrap';

class CustomerCard extends Component{

    constructor(props){
        super(props);
        this.state = {
            cus_id: props.customerNum,
            first_name: "",
            last_name: "",
            email: "",
            phone: "",
            bill_pref: "",
            address: "",
            city: "",
            state: "",
            zip: ""
        };
    }

    componentDidMount() {
        let customerDetailURL = 'http://localhost:8080/customer/' + this.state.cus_id;
        axios.get(customerDetailURL)
            .then(res => {
                // console.log("Customer Data for id ",this.state.cus_id," :",res.data);
                let phone = res.data.cus_phone
                let phoneOut;
                if(phone === "nophonenum"){
                    phoneOut = "None"
                }else{
                    phoneOut = "(" + phone.slice(0,3) + ") " + phone.slice(3,6) + "-" + phone.slice(6)
                }
                let email = res.data.cus_email
                let emailOut;
                if(email === "none"){
                    emailOut = "None"
                }else{
                    emailOut = email
                }
                this.setState({
                    first_name: res.data.cus_first_name,
                    last_name: res.data.cus_last_name,
                    email: emailOut,
                    phone: phoneOut,
                    bill_pref: res.data.bill_type,
                    address: res.data.cus_address,
                    city: res.data.cus_city,
                    state: res.data.cus_state,
                    zip: res.data.cus_zip
                })
            })
            .catch(error => {
                console.log(error)
            })
    }

    render() {

        let fullName = this.state.first_name + " " + this.state.last_name;
        let addressBelow = this.state.city +
            ", " + this.state.state + " " + this.state.zip;


        return(
                <Card className={'detail_card'}>
                    <Card.Header><b> {fullName} </b></Card.Header>
                    <Card.Body>
                        <div className={'card_half_column'}>
                            <Card.Title>Billing Address</Card.Title>
                            <Card.Text>{this.state.address}</Card.Text>
                            <Card.Text>{addressBelow}</Card.Text>
                            <Card.Title>Billing Preference</Card.Title>
                            <Card.Text>{this.state.bill_pref}</Card.Text>
                        </div>
                        <div className={'card_half_column'}>
                            <Card.Title>Phone Number</Card.Title>
                            <Card.Text>{this.state.phone}</Card.Text>
                            <Card.Title>Email Address</Card.Title>
                            <Card.Text>{this.state.email}</Card.Text>
                        </div>
                    </Card.Body>
                </Card>

        )
    }
}

export default CustomerCard;