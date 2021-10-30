import React, {Component} from 'react';
import ReactDOM from 'react-dom';
import axios from "axios";
import 'bootstrap/dist/css/bootstrap.min.css';
import './customerlist.css';
import {ListGroup, Button} from "react-bootstrap";
import CustomerCard from "./customerCard";
import LocationCard from "./locationCard";

class CustomerList extends Component {

    constructor(props) {
        super(props);

        this.state = {
            customerData: [],
        }
    }

    createNewCustomer(){
        console.log("Create a new customer")
    }

    pullCustomerDetail(cusId){
        console.log("Customer selected id = ",cusId)
        const customerCard = (
            <CustomerCard customerNum={cusId}/>
        );
        ReactDOM.render(customerCard,document.getElementById('detail_column'))
        const locationCard = (
            <LocationCard customerNum={cusId}/>
        )
    }


    componentDidMount() {
        console.log("Pulling all customers")
        this.pullAllCustomerData()

    }

    pullAllCustomerData(){
        let customerList = [];
        axios.get('http://localhost:8080/customer_all')
            .then(response => {
                // Populate the customerList array
                for(let x = 0; x < response.data.length; x++){
                    customerList[x] = response.data[x];
                }
                this.setState({customerData: customerList})
            })
            .catch(error => {
                console.log(error);
            });

    }

    render(){

        return(
            <React.Fragment>
                <Button id={"new-customer-button"}
                        variant={"primary"}
                        onClick={event => this.createNewCustomer()}>
                    + New Customer
                </Button>
                <ListGroup className={"customer-list-group"}>
                    {this.state.customerData.map(customer => (
                        <ListGroup.Item eventKey={customer.cus_id}
                                        className={"single-customer-item"}
                                        // Don't leave out the event, otherwise it goes nuts
                                        onClick={event => this.pullCustomerDetail(customer.cus_id)}>
                            {customer.cus_first_name}
                        </ListGroup.Item>
                    ))}
                </ListGroup>
            </React.Fragment>
        )
    }

}

export default CustomerList;