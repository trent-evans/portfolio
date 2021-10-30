import React, {Component} from 'react';
import axios from 'axios';
import 'bootstrap/dist/css/bootstrap.min.css';
import {Card, Table} from "react-bootstrap";
import './detailcards.css';

class LocationCard extends Component{

    constructor(props){
        super(props);

        this.state = {
            cus_id: props.customerNum,
            locsDetails: []
        }
    }

    componentDidMount() {
        let dataList = [];
        let locDataURL = 'http://localhost:8080/customer_prop/' + this.state.cus_id;
        axios.get(locDataURL)
            .then(res => {
                // console.log("Data pulled :",res.data)
                for(let x = 0; x < res.data.details.length; x++){
                    dataList[x] = res.data.details[x];
                }
                this.setState({locsDetails: dataList})
            })
            .catch(err => {
                console.log(err);
            });
    }

    render(){

        let locDetails;
        if(this.state.locsDetails){
            locDetails =
                this.state.locsDetails.map(({address, jobs}, x) => (
                    <Card.Body>
                        <Card.Title>{address}</Card.Title>
                        <Table>
                            <thead>
                                <tr>
                                    <th>Service</th>
                                    <th>Date Planned</th>
                                    <th>Completed</th>
                                    <th>Charge ($)</th>
                                </tr>
                            </thead>
                            <tbody>
                            {jobs.map(job => (
                                <tr>
                                    <td>{job.name}</td>
                                    <td>{job.date_planned}</td>
                                    <td>{job.date_complete}</td>
                                    <td>{job.charge}</td>
                                </tr>
                            ))}
                            </tbody>
                        </Table>
                    </Card.Body>
                ))
        }else{
            locDetails = <Card.Title>Loading...</Card.Title>
        }

        return(
            <Card className={"detail_card"}>
                <Card.Header><b>Property Details</b></Card.Header>
                <Card.Body>{locDetails}</Card.Body>
            </Card>
        )




    }


}

export default LocationCard;