import React, { Component } from "react";
import { hot } from "react-hot-loader";
import { Navbar, Nav } from 'react-bootstrap';
import { Link } from "react-router-dom";
import logo from "../public/images/itmatters.png";

class Navigation extends Component {
  render() {
    return(
      <div className="Navigation">
        <Navbar className="navbar-custom" expand="lg">
          <Navbar.Brand href="/"><img src={logo} className="d-inline-block align-top" height="30" alt="Broadcom"/></Navbar.Brand>
          <Navbar.Toggle aria-controls="basic-navbar-nav" />
        </Navbar>
      </div>
    );
  }
}

export default hot(module)(Navigation);
