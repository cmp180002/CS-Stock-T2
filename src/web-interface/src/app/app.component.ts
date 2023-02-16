import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.less']
})
export class AppComponent {
  title = 'web-interface';

  isMobile(): boolean{
    return window.innerWidth < 800;
  }
}
