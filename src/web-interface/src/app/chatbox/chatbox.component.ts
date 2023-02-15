import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-chatbox',
  templateUrl: './chatbox.component.html',
  styleUrls: ['./chatbox.component.less']
})
export class ChatboxComponent implements OnInit {

  value = "";

  constructor() { }

  ngOnInit(): void {
  }

  update(value: string){
    this.value = value;
  }
}
