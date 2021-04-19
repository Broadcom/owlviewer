import React, { Component } from "react";
import { hot } from "react-hot-loader";
import "./App.scss";
import Upload from "./Upload.js";
import OntologyViewer from "./OntologyViewer.js";
import Navigation from "./Navigation.js";
import { Container, Row, Col } from "react-bootstrap";
import { HashRouter as Router, Switch, Link, Route } from "react-router-dom";

class App extends Component {
  render() {
    return(
      <Router>
        <div className="App">
          <Container className="vh-100 d-flex flex-column" style={{maxWidth: "100%"}}>
            <Row>
              <Col style={{paddingRight: 0, paddingLeft: 0}}>
                <Navigation />
              </Col>
            </Row>
            <Row className="h-100">
              <Col className="center-container">
                <Switch>
                  <Route exact path="/" component={OntologyViewer}/>
                </Switch>
              </Col>
            </Row>
          </Container>
        </div>
      </Router>
    );
  }
}

export default hot(module)(App);
