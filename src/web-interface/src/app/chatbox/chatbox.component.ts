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

  getSubmission(currentMessage: string){
    let date = new Date();
    /*var baseString = "User: The current date is " + (date.toDateString());
    this.messages.forEach(message => {
      if(message.sentUser){
        baseString += "User:" + message.content + "\n";
      } else {
        baseString += "->" + message.content + "\n";
      }
    })
    baseString += "User: " + currentMessage + "->";*/
    return "Today is " + date.toDateString() + ". " + currentMessage + "->";
  }

  invalidStrings = ["Today is", "What", "->", "Join the conv", "Which"]
  checkValid(str: string): boolean{
    if(str == "")
      return false;
    
    var isNotUnique = false;
    for(var i = 0; i < this.messages.length; i++){
      if(this.messages[i].content == str){
        isNotUnique = true;
      }
    }
    if(isNotUnique){
      return false;
    }

    var isValid = true;
    this.invalidStrings.forEach((invalidString) => {
      if(str.includes(invalidString)) isValid = false;
    })
    return isValid;
  }

  submit(input: HTMLInputElement){
    if(input.value != "" && !this.awaitingResponse){
      this.messages.push({sentUser: true, content: input.value});
      this.awaitingResponse = true;

      this.botResponseService.getResponse(this.getSubmission(input.value)).then((response) => {
        var modifiedResponse = "";
        if(response != undefined){
          modifiedResponse = response//.generated_text;
        }
        
        var splitResponses = modifiedResponse.split(/[\n]+/)

        console.log(splitResponses)
        splitResponses.forEach(responseString => {
          if(this.checkValid(responseString)){
            this.messages.push({sentUser: false, content: responseString});
          }
        })

        this.awaitingResponse = false;
      })

      input.value = "";
      this.update("");
    }
  }

  constructor(private botResponseService: BotResponseService) { }
}
