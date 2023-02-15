import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { InfoboxComponent } from './infobox/infobox.component';
import { ChatboxComponent } from './chatbox/chatbox.component';

@NgModule({
  declarations: [
    AppComponent,
    InfoboxComponent,
    ChatboxComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
