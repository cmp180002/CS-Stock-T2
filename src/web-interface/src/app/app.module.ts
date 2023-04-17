import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations"
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { InfoboxComponent } from './infobox/infobox.component';
import { ChatboxComponent } from './chatbox/chatbox.component';
import { MobileSiteComponent } from './mobile-site/mobile-site.component';
import { DesktopSiteComponent } from './desktop-site/desktop-site.component';


@NgModule({
  declarations: [
    AppComponent,
    InfoboxComponent,
    ChatboxComponent,
    MobileSiteComponent,
    DesktopSiteComponent
  ],
  imports: [
    HttpClientModule,
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
