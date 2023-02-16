import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DesktopSiteComponent } from './desktop-site/desktop-site.component';
import { MobileSiteComponent } from './mobile-site/mobile-site.component';

const routes: Routes = [
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
