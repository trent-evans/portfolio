import React from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';
import {
    Tabs,
    Tab,
    Card,
    Button,
    ListGroup,
    Accordion,
} from "react-bootstrap";
import 'reactjs-popup/dist/index.css'
import axios from 'axios';
import ReactDOM, {unmountComponentAtNode} from 'react-dom';
import CustomerCard from "./customComponents/customerCard";
import LocationCard from "./customComponents/locationCard";
import DetailCards from "./customComponents/detailCards";
import PaymentList from "./customComponents/paymentList";
import JobForm from "./customComponents/jobForm";

class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            landID: 1,
            activeCustomerId: 14,
            monday: [],
            tuesday: [],
            wednesday: [],
            thursday: [],
            friday: [],
            saturday: [],
            customerList: ["Jim"],
            paymentDetail: []
        }
    }

    makeBillCall() {
        axios.get('http://localhost:8080/bills')
            .then(res => {
                console.log(res.bill_count," bills generated");
            })
            .catch(err => {
                console.log(err);
            });
    }

    getCustomerList(){
        let customerData = [];
        axios.get('http://localhost:8080/customer_all')
            .then(res => {
                for(let x = 0; x < res.data.length; x++) {
                    customerData[x] = res.data[x];
                }
                this.setState({customerList:customerData})
            })
            .catch(err => {
               console.log("Error pulling customer data:\n",err)
            });
    }

    getWeeklySchedule(){
        let scheduleUrl = `http://localhost:8080/schedule/` + this.state.landID
        let week = [6]
        axios.get(scheduleUrl)
            .then(res => {
                if(res.data.monday) {
                    week[0] = res.data.monday;
                }
                if(res.data.tuesday) {
                    week[1] = res.data.tuesday;
                }
                if(res.data.wednesday) {
                    week[2] = res.data.wednesday;
                }
                if(res.data.thursday) {
                    week[3] = res.data.thursday;
                }
                if(res.data.friday) {
                    week[4] = res.data.friday;
                }
                if(res.data.saturday) {
                    week[5] = res.data.saturday;
                }
                this.setState({monday:week[0],
                    tuesday:week[1],
                    wednesday:week[2],
                    thursday:week[3],
                    friday:week[4],
                    saturday:week[5]
                })
            })
            .catch( err => {
                console.log(err);
            });
    }

    getPaymentsDue(){
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
        this.getCustomerList()
        this.getWeeklySchedule()
        this.getPaymentsDue()
    }


    // This function looks super gross
    // Unfortunately, none of this works if these things are missing
    // I don't know why that is the case, but it is
    // Ergo, the function remains as it is
    async selectActiveCustomer(cusId){
        let customerCard = document.getElementById("customer_card")
        if(customerCard){
            unmountComponentAtNode(customerCard)
        }
        let locationCard = document.getElementById("location_card")
        if(locationCard){
            unmountComponentAtNode(locationCard)
        }
        let detailCards = document.getElementById("detail_card")
        if (detailCards){
            unmountComponentAtNode(detailCards)
        }
        if(cusId === this.state.activeCustomerId){
            return
        }

        // I don't know why, but this axios request is integral to this thing working
        // It doesn't do a thing.  But if I take it out everything breaks
        let locIds = [];
        let requestURL = 'http://localhost:8080/loc_id_by_cus_id/' + cusId
        await axios.get(requestURL)
            .then(res => {
                for (let x = 0; x < res.data.length; x++) {
                    locIds[x] = res.data[x].id
                }
            })
            .catch(err => {
               console.log(err)
            });

        ReactDOM.render(<CustomerCard id = "customer_card" customerNum={cusId}/>, document.getElementById('detail_column'))
        ReactDOM.render(<LocationCard id = "location_card" customerNum={cusId}/>, document.getElementById('detail_column'))
        ReactDOM.render(<DetailCards id = "detail_card" customerNum={cusId}/>,document.getElementById('detail_column'))
        this.setState({activeCustomerId:cusId})
    }

    render() {

        // Massive duplicated code chunk
        // The smart thing to do would be to move all of this into an array
        // I will do this at some point, but not right now
        // Because it works this way, so we'll worry about how to optimize it
        // when I'm not under a major time crunch to get this turned in
        let mondayList;
        let mondayDisabled = true;
        if (this.state.monday) {
            mondayDisabled = false
            mondayList =
               <Accordion className={"job_accordian"}>
                   {this.state.monday.map(job => (
                   <Card>
                        <Accordion.Toggle eventKey={job.job_id} as={Card.Header}>
                            {job.customer_name}
                        </Accordion.Toggle>
                       <Accordion.Collapse eventKey={job.job_id}>
                           <JobForm job_id={job.job_id}
                                    date={job.date}
                                    complete = {job.complete}
                                    desc = {job.job_desc}/>
                       </Accordion.Collapse>
                   </Card>
                   ))}
               </Accordion>
        }else{
            mondayList = <ListGroup.Item className={"no_job"} active disabled={true}>
                No work today
            </ListGroup.Item>
        }

        let tuesdayList;
        let tuesdayDisabled = true;
        if (this.state.tuesday) {
            tuesdayDisabled = false;
            tuesdayList =
                <Accordion className={"job_accordian"}>
                {this.state.tuesday.map(job => (
                    <Card>
                        <Accordion.Toggle eventKey={job.job_id} as={Card.Header}>
                            {job.customer_name}
                        </Accordion.Toggle>
                        <Accordion.Collapse eventKey={job.job_id}>
                            <JobForm job_id={job.job_id}
                                     date={job.date}
                                     complete = {job.complete}
                                     desc = {job.job_desc}/>
                        </Accordion.Collapse>
                    </Card>
                ))}
            </Accordion>
        }else{
            tuesdayList = <ListGroup.Item className={"no_job"} active disabled={true}>
                No work today
            </ListGroup.Item>
        }

        let wednesdayList;
        let wednesdayDisabled = true;
        if (this.state.wednesday) {
            wednesdayDisabled = false;
            wednesdayList = <Accordion className={"job_accordian"}>
                {this.state.wednesday.map(job => (
                    <Card>
                        <Accordion.Toggle eventKey={job.job_id} as={Card.Header}>
                            {job.customer_name}
                        </Accordion.Toggle>
                        <Accordion.Collapse eventKey={job.job_id}>
                            <JobForm job_id={job.job_id}
                                     date={job.date}
                                     complete = {job.complete}
                                     desc = {job.job_desc}/>
                        </Accordion.Collapse>
                    </Card>
                ))}
            </Accordion>
        }else{
            wednesdayList = <ListGroup.Item className={"no_job"} active disabled={true}>
                No work today
            </ListGroup.Item>
        }

        let thursdayList;
        let thursdayDisabled = true;
        if (this.state.thursday) {
            thursdayDisabled = false;
            thursdayList = <Accordion className={"job_accordian"}>
                {this.state.thursday.map(job => (
                    <Card>
                        <Accordion.Toggle eventKey={job.job_id} as={Card.Header}>
                            {job.customer_name}
                        </Accordion.Toggle>
                        <Accordion.Collapse eventKey={job.job_id}>
                            <JobForm job_id={job.job_id}
                                     date={job.date}
                                     complete = {job.complete}
                                     desc = {job.job_desc}/>
                        </Accordion.Collapse>
                    </Card>
                ))}
            </Accordion>
        }else{
            thursdayList = <ListGroup.Item className={"no_job"} active disabled={true}>
                No work today
            </ListGroup.Item>
        }

        let fridayList;
        let fridayDisabled = true;
        if (this.state.friday) {
            fridayDisabled = false;
            fridayList = <Accordion className={"job_accordian"}>
                {this.state.friday.map(job => (
                    <Card>
                        <Accordion.Toggle eventKey={job.job_id} as={Card.Header}>
                            {job.customer_name}
                        </Accordion.Toggle>
                        <Accordion.Collapse eventKey={job.job_id}>
                            <JobForm job_id={job.job_id}
                                     date={job.date}
                                     complete = {job.complete}
                                     desc = {job.job_desc}/>
                        </Accordion.Collapse>
                    </Card>
                ))}
            </Accordion>
        }else{
            fridayList = <ListGroup.Item className={"no_job"} active disabled={true}>
                No work today
            </ListGroup.Item>
        }

        let saturdayList;
        let saturdayDisabled = true;
        if (this.state.saturday) {
            saturdayDisabled = false;
            saturdayList = <Accordion className={"job_accordian"}>
                {this.state.saturday.map(job => (
                    <Card>
                        <Accordion.Toggle eventKey={job.job_id} as={Card.Header}>
                            {job.customer_name}
                        </Accordion.Toggle>
                        <Accordion.Collapse eventKey={job.job_id}>
                            <JobForm job_id={job.job_id}
                                     date={job.date}
                                     complete = {job.complete}
                                     desc = {job.job_desc}/>
                        </Accordion.Collapse>
                    </Card>
                ))}
            </Accordion>
        }else{
            saturdayList = <ListGroup.Item className={"no_job"} active disabled={true}>
                No work today
            </ListGroup.Item>
        }

        return (
            <div>
                <div>
                    <Card bg={'success'} id={'header_card'}>
                        <Card.Header>Welcome 2T's Lawn Care</Card.Header>
                        <Card.Body>
                            <Button id={"billButton"} onClick={event => (this.makeBillCall())}>Generate Bills</Button>
                        </Card.Body>
                    </Card>
                </div>

                <div>
                    <Tabs defaultActiveKey="schedule" id='main_tabs'>
                        <Tab eventKey="schedule" title="Schedule">
                            {/*<Button onClick={event => this.postNewJob()}>+ New Job</Button>*/}
                            {/*Insert forward and back buttons here*/}
                            <div id={"schdule"}>
                                <div className={'day_column'} id={'Monday'}>
                                    Monday
                                    <br/>
                                    <ListGroup className={'job_list'}>
                                        {mondayList}
                                    </ListGroup>
                                </div>
                                <div className={'day_column'} id={'Tuesday'}>
                                    Tuesday
                                    <br/>
                                    <ListGroup className={'job_list'}>
                                        {tuesdayList}
                                    </ListGroup>
                                </div>
                                <div className={'day_column'} id={'Wednesday'}>
                                    Wednesday
                                    <br/>
                                    <ListGroup className={'job_list'}>
                                        {wednesdayList}
                                    </ListGroup>
                                </div>
                                <div className={'day_column'} id={'Thursday'}>
                                    Thursday
                                    <br/>
                                    <ListGroup className={'job_list'}>
                                        {thursdayList}
                                    </ListGroup>
                                </div>
                                <div className={'day_column'} id={'Friday'}>
                                    Friday
                                    <br/>
                                    <ListGroup className={'job_list'}>
                                        {fridayList}
                                    </ListGroup>
                                </div>
                                <div className={'day_column'} id={'Saturday'}>
                                    Saturday
                                    <br/>
                                    <ListGroup className={'job_list'}>
                                        {saturdayList}
                                    </ListGroup>
                                </div>
                            </div>
                        </Tab>
                        <Tab eventKey="customers" title="Customers">
                            <div id={'customer_column'}>
                                <ListGroup id={"customer_list"} className={"customer_tab_columns"}>
                                    {this.state.customerList.map(customer => (
                                            <ListGroup.Item eventKey={customer.cus_id}
                                                className={"single-customer-item"}
                                                onClick={event => this.selectActiveCustomer(customer.cus_id)}>
                                                {customer.cus_first_name}
                                            </ListGroup.Item>
                                        ))}
                                </ListGroup>
                            </div>
                            <div id={'detail_column'} className={"customer_tab_columns"}/>

                        </Tab>
                        <Tab eventKey="payment" title="Payments">
                            <PaymentList id = 'payment_list' land_id={this.state.landID}/>
                        </Tab>
                    </Tabs>
                </div>
            </div>

        );
    }
}

export default App;

ReactDOM.render(<App/>,document.getElementById("root"))
