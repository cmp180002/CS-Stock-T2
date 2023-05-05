import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http"
import { Observable } from 'rxjs';
import { Configuration, OpenAIApi } from 'openai';


@Injectable({
  providedIn: 'root'
})
export class BotResponseService {
  configuration = new Configuration({
    apiKey: this.rot13("sk-TnH2suP1e3u53tKRnOFcG3OlbkSWUNA7XVe1OMyYxqWVVhNy")
  });
  openai = new OpenAIApi(this.configuration);

  rot13(str: string): string {
    var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    return str.split("").reduce(function(a, b) {
      if (chars.indexOf(b) == -1) {
        return a + b;
      }
      return a + chars[(chars.indexOf(b)+13) % chars.length]
    }, "");
  }

  async getResponse(input: string): Promise<string>{
    const completion = await this.openai.createCompletion({
      model: "davinci:ft-csstockt2utd-2023-05-05-19-17-58",
      prompt: input,
      max_tokens: 150,
      top_p: .4,
    });

    var baseText = completion.data.choices[0].text;
    if(baseText != undefined){
      return baseText;
    }
    return "";
  }

  constructor(private http: HttpClient) {
    delete this.configuration.baseOptions.headers['User-Agent'];
   }
}
