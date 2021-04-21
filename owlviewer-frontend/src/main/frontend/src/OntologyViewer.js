import React, { Component } from "react";
import { ReactCytoscape } from "react-cytoscape";
import SplitPane from "react-split-pane";
import { hot } from "react-hot-loader";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faImage } from "@fortawesome/free-regular-svg-icons";
import ClassPropertiesTable from "./ClassPropertiesTable.js";
import axios from "axios";

// Memoize ReactCytoscape component and make sure it is only gets re-rendered if its elements have changed
const Cytoscape = React.memo(props => {
  console.log("Render ReactCytoscape");
  return(
    <ReactCytoscape
      containerID={props.containerID}
      elements={props.elements}
      cyRef={props.cyRef}
      cytoscapeOptions={props.cytoscapeOptions}
      style={props.style}
      layout={props.layout} />
  )
}, (prevProps, nextProps) => {
  if (prevProps.elements === nextProps.elements) {
    console.log("element are equal");
    return true;
  }
  else {
    console.log("elements are NOT equal");
    return false;
  }
});

class OntologyViewer extends Component {

  state = {
    selectedNodeId: null,
    selectedNodeLabel: null,
    elements: {
      nodes: [],
      edges: []
    }
  }

  constructor(props) {
    super(props);
    this.updateGraphSize = this.updateGraphSize.bind(this);
    this.cyRef = this.cyRef.bind(this);
    this.getStyle = this.getStyle.bind(this);
    this.getElements = this.getElements.bind(this);
    this.onClickNode = this.onClickNode.bind(this);
    this.selectNode = this.selectNode.bind(this);
    this.downloadGraph = this.downloadGraph.bind(this);
    this.panelWidth = 506;
  }

  componentDidMount() {
    window.addEventListener("resize", this.updateGraphSize.bind(this));
    this.getElements();
  }

  getElements() {
    axios.get(window.backendUrl + "/ontologies/graph").then(
      result => {
        var elements = {
          nodes: [],
          edges: []
        }
        if (result.hasOwnProperty("data") && result.data.hasOwnProperty("vertices")) {
          elements.nodes = [];
          for (var i = 0; i < result.data.vertices.length; i++) {
            elements.nodes.push({"data": result.data.vertices[i]});
          }
        }
        if (result.hasOwnProperty("data") && result.data.hasOwnProperty("edges")) {
          elements.edges = [];
          for (var i = 0; i < result.data.edges.length; i++) {
            elements.edges.push({"data": {
                "id": result.data.edges[i].source + result.data.edges[i].id + result.data.edges[i].target,
                "source": result.data.edges[i].source,
                "target": result.data.edges[i].target,
                "uri": result.data.edges[i].id,
                "label": result.data.edges[i].label
              }
            });
          }
        }
        this.setState({elements: elements});
        console.log("retrieved graph data from backend");
      },
      error => {
        console.error("Failed to load data from backend");
      }
    );
  }

  updateGraphSize(offset) {
    if (typeof offset !== 'undefined' && !isNaN(offset)) {
        this.panelWidth = offset;
    }
    document.getElementById('cytoscape').style.width = window.innerWidth - (6 + this.panelWidth) + "px";
  }

  getStyle() {
    return [
      {
	    selector: 'node',
	    css: {
          "content": function (ele) { return ele.data("label") || ele.data("uri") },
          "font-size": "10",
          "width": "20",
          "height": "20",
          "background-color": "#005c8a",
          "text-halign": "right",
          "text-valign": "center"
        }
      },
      {
	    selector: 'node.selected',
	    css: {
          "content": function (ele) { return ele.data("label") || ele.data("uri") },
          "font-size": "10",
          "width": "20",
          "height": "20",
          "background-color": "#cc092f",
          "text-halign": "right",
          "text-valign": "center"
        }
      },
      {
        selector: 'edge',
        css: {
          "label": function (ele) { return ele.data("label") || ele.data("uri") },
          "font-size": "8px",
          "width": "1",
          "line-style": function (ele) { return ele.data("uri") === "http://www.w3.org/2000/01/rdf-schema#subClassOf" ? "dashed" : "solid" },
          "curve-style": "bezier",
          "target-arrow-shape": "triangle",
          "target-arrow-fill": function (ele) { return ele.data("uri") === "http://www.w3.org/2000/01/rdf-schema#subClassOf" ? "hollow" : "filled" },
        }
      },
      {
        selector: ':selected',
        css: {
          "overlay-color": "blue",
          "overlay-opacity": "0.5",
          "overlay-padding": "5",
          "background-color": "red",
          "line-color": "red",
          "target-arrow-color": "red",
          "source-arrow-color": "red"
        }
      }
    ]
  }

  onClickNode(evt) {
	var node = evt.target;
	this.selectNode(node.id());
  }

  selectNode(nodeId) {
  console.log("select " + nodeId);
    var node = this.cy.getElementById(nodeId);
	if (this.state.selectedNodeId != null && this.state.selectedNodeId != node.id()) {
	  this.cy.getElementById(this.state.selectedNodeId).removeClass("selected");
	}
	if (this.selectedNodeId != node.id()) {
	  this.state.selectedNodeId = node.id();
	  this.state.selectedNodeLabel = node.data("label");
	  this.setState({selectedNodeId: node.id()});
	  node.addClass("selected");
	}
  }

  downloadGraph() {
    var data = new Blob([this.cy.png({"output": "blob"})], {type: "image/png"});
    const anchor = document.createElement('a');
    anchor.href = window.URL.createObjectURL(data);
    anchor.target = "_blank";
    anchor.download = "ontology.png";
    anchor.click();
  }

  cyRef(cy) {
    this.cy = cy;
    cy.on('click', 'node', (evt) => this.onClickNode(evt));
  }

  render() {
    const { selectedNodeId, selectedNodeLabel, elements } = this.state;

    var details;
    if (selectedNodeId != null) {
      details = (
        <div>
          <div className="detailsHeader">
            {selectedNodeLabel != null ? selectedNodeLabel : selectedNodeId.split("#")[1]}<br/><span className="iri">{selectedNodeId}</span>
          </div>
          <ClassPropertiesTable selectionHandler={this.selectNode} selectedNodeIRI={selectedNodeId}/>
        </div>);
    }
    else {
      details = <p>Please select a node!</p>;
    }

    console.log("Render OntologyViewer");
    return(
      <div className={this.props.className} style={this.props.style}>
        <SplitPane split="vertical" minSize={500} primary="second" onChange={(size) => this.updateGraphSize(size)}>
          <div>

            <Cytoscape containerID="cytoscape"
              elements={elements}
              cyRef={(cy) => {this.cyRef(cy)}}
              cytoscapeOptions={{wheelSensitivity: 0.1}}
              style={this.getStyle()}
              layout={{name: 'dagre'}} />

            <button type="button" onClick={this.downloadGraph}
                style={{position: "absolute", bottom: 20, left: 20, fontSize: "1rem", borderColor: "black", backgroundColor: "white", whiteSpace: "nowrap"}} className="btn btn-default btn-sm">
              <FontAwesomeIcon icon={faImage}/>  Download as PNG
            </button>

           </div>
           <div>
             {details}
           </div>
        </SplitPane>
      </div>
    );
  }
}

export default hot(module)(OntologyViewer);
