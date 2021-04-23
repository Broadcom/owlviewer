import React from "react";
import createClass from 'create-react-class';
import Table from "react-bootstrap/Table";
import BootstrapTable from "react-bootstrap-table-next";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMinusSquare, faPlusSquare } from "@fortawesome/free-solid-svg-icons";
import { hot } from "react-hot-loader";
import axios from "axios";

class ClassPropertiesTable extends React.Component {

  state = {
    properties: []
  }

  constructor(props) {
    super(props);
    this.getClassProperties = this.getClassProperties.bind(this);
    this.getColumns = this.getColumns.bind(this);
    this.cardinalityFormatter = this.cardinalityFormatter.bind(this);
    this.propertyValueFormatter = this.propertyValueFormatter.bind(this);
    this.getRowStyle = this.getRowStyle.bind(this);
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

  cardinalityFormatter(cell, row) {
    if (row.hasOwnProperty("types") && row.types.length > 0) {
      var rows = [];
      for (var typeIdx = 0; typeIdx < row.types.length; typeIdx++) {
        if (row.types[typeIdx].minCardinality === row.types[typeIdx].maxCardinality) {
          rows.push(<span>[{row.types[typeIdx].minCardinality}]<br/></span>);
        }
        else if (row.types[typeIdx].minCardinality != null && row.types[typeIdx].maxCardinality == null) {
          rows.push(<span>[{row.types[typeIdx].minCardinality}..*]<br/></span>);
        }
        else if (row.types[typeIdx].minCardinality == null && row.types[typeIdx].maxCardinality != null) {
          rows.push(<span>[*..{row.types[typeIdx].maxCardinality}]<br/></span>);
        }
        else if (row.types[typeIdx].minCardinality != null && row.types[typeIdx].maxCardinality != null) {
          rows.push(<span>[{row.types[typeIdx].minCardinality}..{row.types[typeIdx].maxCardinality}]<br/></span>);
        }
        else {
          rows.push(<span><br/></span>);
        }
      }
      return <div>{rows}</div>
    }
  }

  propertyValueFormatter(cell, row) {
    if (row.hasOwnProperty("types") && row.types.length > 0) {
      var rows = [];
      for (var typeIdx = 0; typeIdx < row.types.length; typeIdx++) {
          if (row.literal) {
            rows.push(<span>{row.types[typeIdx].name}<br/></span>);
          }
          else {
            var iri = row.types[typeIdx].iri;
            rows.push(<a href="#" role="button" onClick={() => {this.props.selectionHandler(iri)}}>{row.types[typeIdx].name}<br/></a>);
          }
      }
      return <div>{rows}</div>
    }
  }

  getRowStyle(row, rowIndex) {
    var style = {};
    if (row.hasOwnProperty("types") && row.types.length > 1) {
      style.lineHeight = "1.7rem";
    }
    return style;
  }

  getColumns() {
    return [
      {
        dataField: "name",
        text: "Property",
        style: {
          width: "45%"
        },
        sort: true
      },
      {
        dataField: "cardinality",
        text: "",
        style: {
          width: "10%"
        },
        align: "right",
        formatter: this.cardinalityFormatter,
      },
      {
        dataField: "types.name",
        text: "Type",
        style: {
          width: "45%"
        },
        formatter: this.propertyValueFormatter
      }
    ];
  }

  render() {
    const expandRow = {
      renderer: row => (
        <div>
          <p><b>Internationalised Resource Identifier (IRI):</b><br/>{row.iri}</p>
          { row.description != null ? <p><b>Description:</b><br/>{row.description}</p> : "" }
        </div>
      ),
      showExpandColumn: true,
      expandHeaderColumnRenderer: ({ isAnyExpands }) => {
        if (isAnyExpands) {
          return <FontAwesomeIcon style={{color: "#949494"}} icon={faMinusSquare}/>;
        }
        return <FontAwesomeIcon style={{color: "#949494"}} icon={faPlusSquare}/>;
      },
      expandColumnRenderer: ({ expanded }) => {
        if (expanded) {
          return <FontAwesomeIcon style={{color: "#949494"}} icon={faMinusSquare}/>;
        }
        return <FontAwesomeIcon style={{color: "#949494"}} icon={faPlusSquare}/>;
      }
    }

    const defaultSorted = [{
      dataField: "name",
      order: "asc"
    }];

    return (
      <div className={this.props.className} style={this.props.style}>
        <BootstrapTable
          keyField="name"
          data={this.state.properties}
          columns={this.getColumns()}
          bordered={false}
          rowStyle={this.getRowStyle}
          expandRow={expandRow}
          defaultSorted={defaultSorted}
          condensed/>
      </div>
    );
  }
}

export default hot(module)(ClassPropertiesTable);