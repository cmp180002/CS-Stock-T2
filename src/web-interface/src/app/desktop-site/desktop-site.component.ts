import { Component, OnInit } from '@angular/core';
import { InfoboxComponent	 } from '../infobox/infobox.component';
import { ChatboxComponent } from '../chatbox/chatbox.component';

@Component({
  selector: 'app-desktop-site',
  templateUrl: './desktop-site.component.html',
  styleUrls: ['./desktop-site.component.less']
})
export class DesktopSiteComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
