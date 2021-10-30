import React, {Component} from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import CustomerCard from "./customerCard";
import LocationCard from "./locationCard";

class DetailCards extends Component {

    constructor(props) {
        super(props);
        this.state = {
            cus_id: props.customerNum
        }
    }

    render() {
        return (
            <div>
                <CustomerCard id = "customer_card" customerNum={this.state.cus_id}/>
                <LocationCard id = "location_card" customerNum={this.state.cus_id}/>
            </div>
        )
    }

}

export default DetailCards;