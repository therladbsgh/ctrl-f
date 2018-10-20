import React, { Component } from 'react';
import axios from 'axios';

import logo from './logo.svg';
import './App.css';

class App extends Component {
  constructor(props) {
    super(props);

    this.state = {
      word: "",
      file: "",
      imgWidth: 0,
      imgHeight: 0,
      bbox: [],
    };

    this.handleInputChange = this.handleInputChange.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  handleInputChange(event) {
    const target = event.target;
    const value = target.value;
    const name = target.name;

    this.setState({
      [name]: value,
    });
  }

  handleChange(event) {
    event.persist();
    const data = new FormData();
    data.append('file', event.target.files[0]);
    data.append('word', this.state.word);
    var img = new Image();
    img.onload = () => {
        axios.post( 'http://127.0.0.1:8080/get_boxes', data, {
          headers: {
              'Content-Type': 'multipart/form-data'
            }
          }
        ).then((res) => {
          this.setState({
            file: URL.createObjectURL(event.target.files[0]),
            imgWidth: img.width,
            imgHeight: img.height,
            bbox: res.data
          });
        });
    };
    img.src = URL.createObjectURL(event.target.files[0]);
  }

  render() {
    return (
      <div className="App">
        <p>
          Let's analyze pictures.
        </p>
        <input
          type="text"
          className="form-control"
          name="word"
          value={this.state.word}
          onChange={this.handleInputChange}
        />
        <input type="file" onChange={this.handleChange}/>
        <img src={this.state.file}/>
        <svg
          style={{
            height: `${this.state.imgHeight}px`,
            width: `${this.state.imgWidth}px`
          }}
        >
          <image
            href={this.state.file}
            height={`${this.state.imgHeight}px`}
            width={`${this.state.imgWidth}px`}
          />
          { this.state.bbox.map((box) => {
              return (
                <rect
                  key={`${box.left}${box.top}`}
                  x={box.left}
                  y={box.top}
                  width={box.width}
                  height={box.height}
                  style={{
                    fill: 'blue',
                    stroke: 'green',
                    strokeWidth:2,
                    fillOpacity:0.1,
                    strokeOpacity:0.9
                  }}
                />
              );
            })
          }
        </svg>
      </div>
    );
  }
}

export default App;
