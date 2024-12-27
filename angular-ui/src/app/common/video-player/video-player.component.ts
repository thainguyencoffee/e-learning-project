import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import videojs from "video.js";

@Component({
  selector: 'app-video-player',
  standalone: true,
  imports: [],
  templateUrl: './video-player.component.html',
  styleUrl: './video-player.component.css'
})
export class VideoPlayerComponent implements AfterViewInit, OnDestroy, OnChanges {

  @ViewChild('videoPlayer', { static: false }) videoPlayer!: ElementRef;
  @Input() videoLink!: string;
  player: any;
  @Input() disablePlay: boolean = false;

  ngAfterViewInit(): void {
    this.initializePlayer();
  }

  private initializePlayer(): void {
    this.player = videojs(this.videoPlayer.nativeElement, {
      controls: true,
      autoplay: false,  // Hủy chế độ autoplay
      preload: 'auto',
    });

    this.updateVideoSource();

    if (this.disablePlay) {
      this.disablePlayButton();  // Vô hiệu hóa nút Play nếu disablePlay là true
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.player) {
      if (changes['disablePlay']) {
        if (this.disablePlay) {
          this.disablePlayButton();
        } else {
          this.enablePlayButton();
        }
      }

      if (changes['videoLink'] && !changes['videoLink'].firstChange) {
        this.updateVideoSource();
      }
    }
  }

  private updateVideoSource(): void {
    if (this.player) {
      this.player.src({
        src: this.videoLink,
        type: 'video/mp4'
      });
      this.player.load();
      if (!this.disablePlay) {
        this.player.play();
      }
    }
  }

  private disablePlayButton(): void {
    if (this.player) {
      this.player.pause();
      this.player.currentTime(0);
    }

    const playButton = this.player.controlBar.playToggle;
    if (playButton) {
      playButton.el().setAttribute('disabled', 'true');
      playButton.el().style.pointerEvents = 'none';  // Vô hiệu hóa sự kiện click
    }

    this.player.el().style.pointerEvents = 'none';  // Vô hiệu hóa toàn bộ sự kiện click
  }

  private enablePlayButton(): void {
    const playButton = this.player.controlBar.playToggle;
    if (playButton) {
      playButton.el().removeAttribute('disabled');
      playButton.el().style.pointerEvents = '';  // Bật lại sự kiện click
    }

    this.player.el().style.pointerEvents = '';  // Bật lại sự kiện click
  }

  ngOnDestroy(): void {
    if (this.player) {
      this.player.dispose();
    }
  }
}
