import React from "react";
import createClass from 'create-react-class';
import Table from "react-bootstrap/Table";
import { hot } from "react-hot-loader";
import axios from "axios";

const TableRow = createClass({
  render() {
    return (
      <tr>
        <td>{this.props.row.name}</td>
        <td style={{textAlign: "right"}}>{typeof this.props.row.types !== "undefined" ? (this.props.row.types.length > 0 ? (this.props.row.types[0].minCardinality === this.props.row.types[0].maxCardinality ? ("[" + (this.props.row.types[0].maxCardinality != null ? this.props.row.types[0].maxCardinality : "*") + "]") : ("[" + (this.props.row.types[0].minCardinality != null ? this.props.row.types[0].minCardinality : "*") + ".." + (this.props.row.types[0].maxCardinality != null ? this.props.row.types[0].maxCardinality : "*") + "]")): "") : ""}</td>
        <td>{typeof this.props.row.types !== "undefined" ? (this.props.row.types.length > 0 ? (!this.props.row.literal ? <a href="#" role="button" onClick={() => {this.props.selectionHandler(this.props.row.types[0].iri)}}>{this.props.row.types[0].name}</a> : this.props.row.types[0].name) : "") : ""}</td>
      </tr>
    );
  }
});

const TableBody = createClass({
  render() {
    return (
      <tbody>
        {this.props.data.map((row) => (
          <TableRow selectionHandler={this.props.selectionHandler} row={row} />
        ))}
      </tbody>
    );
  }
});

class ClassPropertiesTable extends React.Component {

  state = {
    properties: []
  }

  constructor(props) {
    super(props);
    this.getClassProperties = this.getClassProperties.bind(this);
  }

  getClassProperties(selectedNodeIRI) {
    var fragments = selectedNodeIRI.split("#");
    if (fragments.length == 2) {
      axios.get(window.backendUrl + "/ontologies/classes/" + fragments[1] + "?namespace=" + fragments[0]).then(
        result => {
          if (result.hasOwnProperty("data") && result.data.hasOwnProperty("properties")) {
            this.setState({properties: result.data.properties});
          }
        },
        error => {
          console.error("Failed to load class properties from backend");
        }
      );
    }
  }

  componentDidMount() {
    this.getClassProperties(this.props.selectedNodeIRI);
  }

  componentDidUpdate(prevProps) {
    if (prevProps.selectedNodeIRI !== this.props.selectedNodeIRI) {
      this.getClassProperties(this.props.selectedNodeIRI);
    }
  }

  render() {
    return (
      <div className={this.props.className} style={this.props.style}>
        <Table size="sm">
          <thead>
            <tr>
              <th style={{width: "45%"}}>Property</th>
              <th style={{width: "10%"}}/>
              <th style={{width: "45%"}}>Type</th>
            </tr>
          </thead>
          <TableBody selectionHandler={this.props.selectionHandler} data={this.state.properties} />
        </Table>
      </div>
    );
  }
}

export default hot(module)(ClassPropertiesTable);