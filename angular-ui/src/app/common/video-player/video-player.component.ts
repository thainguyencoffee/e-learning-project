import {AfterViewInit, Component, ElementRef, Input, OnDestroy, ViewChild} from '@angular/core';
import videojs from "video.js";

@Component({
  selector: 'app-video-player',
  standalone: true,
  imports: [],
  templateUrl: './video-player.component.html',
  styleUrl: './video-player.component.css'
})
export class VideoPlayerComponent implements AfterViewInit, OnDestroy {

  @ViewChild('videoPlayer', { static: false }) videoPlayer!: ElementRef;
  @Input() videoLink!: string;
  player: any;

  startWatchTime = 0;
  totalWatchTime = 0;
  isWatching = false;

  ngAfterViewInit(): void {
    this.player = videojs(this.videoPlayer.nativeElement, {
      controls: true,
      autoplay: false,
      preload: 'auto',
    });

    this.player.src({
      src: this.videoLink,
      type: 'video/mp4'
    });

    // Sự kiện khi phát video
    this.player.on('play', () => {
      if (!this.isWatching) {
        // Lưu thời gian bắt đầu từ lúc phát
        this.startWatchTime = this.player.currentTime();
        this.isWatching = true;
      }
    });

    // Sự kiện khi video bị tạm dừng hoặc kết thúc
    this.player.on(['pause', 'ended'], () => {
      if (this.isWatching) {
        const currentTime = this.player.currentTime();
        const watchDuration = currentTime - this.startWatchTime;

        // Chỉ cập nhật tổng thời gian xem nếu watchDuration dương
        if (watchDuration > 0) {
          this.totalWatchTime += watchDuration;
        }
        this.isWatching = false; // Đặt lại trạng thái
      }
    });

    // Sự kiện khi người dùng tua video
    this.player.on('seeked', () => {
      // Đặt lại thời gian bắt đầu cho lần phát mới
      this.startWatchTime = this.player.currentTime();

      // Nếu video đang phát, cập nhật totalWatchTime
      if (this.isWatching) {
        const currentTime = this.player.currentTime();
        const watchDuration = currentTime - this.startWatchTime;

        // Không cộng dồn totalWatchTime khi chỉ click vào timeline mà không xem
        if (watchDuration > 0) {
          this.totalWatchTime += watchDuration;
        }
      }
    });
  }

  ngOnDestroy(): void {
    if (this.player) {
      this.player.dispose();
    }
  }

}
