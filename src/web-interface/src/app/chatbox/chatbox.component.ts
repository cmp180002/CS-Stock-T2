import { Component, OnInit } from '@angular/core';
import { Message } from '../message';

import {
  trigger,
  style,
  animate,
  transition,
} from '@angular/animations';
import { BotResponseService } from '../bot-response.service';

@Component({
  selector: 'app-chatbox',
  templateUrl: './chatbox.component.html',
  styleUrls: ['./chatbox.component.less'],
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({bottom: '-50%', marginBottom: "-50px"}),
        animate('300ms ease-in-out', style({bottom: '0px', marginBottom: "0px"}))
      ])
    ])
  ]
})
export class ChatboxComponent implements OnInit {
  value = "";
  awaitingResponse = false;

  messages: Message[] = []

  ngOnInit(): void {
  }

  update(value: string){
    this.value = value;
  }

  submit(input: HTMLInputElement){
    if(input.value != "" && !this.awaitingResponse){
      this.messages.push({sentUser: true, content: input.value});
      this.awaitingResponse = true;

      this.botResponseService.getResponse(input.value).then((value) => {
        this.messages.push({sentUser: false, content: value});
        this.awaitingResponse = false;
      })

      input.value = "";
      this.update("");
    }
  }

  constructor(private botResponseService: BotResponseService) { }
}
