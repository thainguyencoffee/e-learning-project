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


  ngAfterViewInit(): void {
    this.initializePlayer();
  }

  private initializePlayer(): void {
    this.player = videojs(this.videoPlayer.nativeElement, {
      controls: true,
      autoplay: false,
      preload: 'auto',
    });

    this.updateVideoSource();
  }


  ngOnChanges(changes: SimpleChanges): void {
    if (changes['videoLink'] && !changes['videoLink'].firstChange) {
      this.updateVideoSource();
    }
  }

  private updateVideoSource(): void {
    if (this.player) {
      this.player.src({
        src: this.videoLink,
        type: 'video/mp4'
      });
      this.player.load();
      this.player.play();
    }
  }

  ngOnDestroy(): void {
    if (this.player) {
      this.player.dispose();
    }
  }

}
