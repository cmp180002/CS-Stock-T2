import { Component, OnInit } from '@angular/core';
import { Message } from '../message';

@Component({
  selector: 'app-chatbox',
  templateUrl: './chatbox.component.html',
  styleUrls: ['./chatbox.component.less']
})
export class ChatboxComponent implements OnInit {
  value = "";
  messages: Message[] = [
    {sentUser: true, content: "Nulla malesuada, turpis et convallis lacinia, erat nunc tincidunt nisl, sit amet blandit magna turpis ac quam."},
    {sentUser: false, content: "Bingus"},
    {sentUser: false, content: "Bangus"},
    {sentUser: true, content: "Byngus"},
    {sentUser: true, content: "Bungus"},
    {sentUser: false, content: "Bengus"}
  ]

  constructor() { }

  ngOnInit(): void {
  }

  update(value: string){
    this.value = value;
  }

  submit(input: HTMLInputElement){
    alert(input.value);
    input.value = "";
    this.update("");
  }
}
