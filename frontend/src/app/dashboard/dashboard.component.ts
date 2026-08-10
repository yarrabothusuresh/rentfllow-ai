import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProfileService } from '../services/profile.service';
import { BusinessProfile } from '../models/business-profile.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  isCollapsed = false;
  toastVisible = false;
  
  businessProfile!: BusinessProfile;
  tempProfile!: BusinessProfile;
  isEditModalOpen = false;

  constructor(private profileService: ProfileService) {}

  ngOnInit() {
    this.profileService.getProfile().subscribe(profile => {
      this.businessProfile = profile;
    });
  }
  
  toggleSidebar() {
    this.isCollapsed = !this.isCollapsed;
  }
  
  showFutureToast() {
    this.toastVisible = true;
    setTimeout(() => {
      this.toastVisible = false;
    }, 3000);
  }

  openEditModal() {
    this.tempProfile = { ...this.businessProfile };
    this.isEditModalOpen = true;
  }

  closeEditModal() {
    this.isEditModalOpen = false;
  }

  saveProfile() {
    this.businessProfile = { ...this.tempProfile };
    this.isEditModalOpen = false;
  }
}

