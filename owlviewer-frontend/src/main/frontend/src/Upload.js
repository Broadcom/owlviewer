import React, { Component } from "react";
import { hot } from "react-hot-loader";
import Dropzone from "react-dropzone";

class Upload extends Component {
  render() {
    return(
      <div className={this.props.className} style={this.props.style}>
        <Dropzone>
          {({ getRootProps, getInputProps }) => (
            <div {...getRootProps({ className: "dropzone" })} style={{position: "relative", height: "100%", width: "100%", backgroundColor: "yellow"}}>
              <input {...getInputProps()} />
              <p style={{position: "absolute", top: "50%", left: "50%", transform: "translate(-50%,-50%)"}}>Drag'n'drop files, or click to select files</p>
            </div>
          )}
        </Dropzone>
      </div>
    );
  }
}

export default hot(module)(Upload);
