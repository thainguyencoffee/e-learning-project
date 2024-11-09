import {Component, OnInit} from '@angular/core';
import { Uppy } from '@uppy/core';
import Dashboard from "@uppy/dashboard/lib/Dashboard";
import AwsS3Multipart from "@uppy/aws-s3";

@Component({
  selector: 'app-uppy',
  standalone: true,
  templateUrl: './uppy.component.html',
  styleUrl: './uppy.component.css'
})
export class UppyComponent {

  // uppy: Uppy;
  //
  // ngOnInit(): void {
  //   this.uppy = new Uppy()
  //     .use(Dashboard, {
  //       inline: true,
  //       target: '#drag-drop-area'
  //     })
  //     .use(AwsS3Multipart, {
  //       createMultipartUpload: async (file) => {
  //         const response = await fetch(`/upload/start?fileName=${file.name}`, { method: 'POST' });
  //         return await response.json();
  //       },
  //       signPart: async (file, { uploadId, key, partNumber }) => {
  //         const response = await fetch(
  //           `/upload/sign-part?key=${key}&uploadId=${uploadId}&partNumber=${partNumber}`,
  //           { method: 'POST' }
  //         );
  //       }
  //
  //
  //     });
  //
  //   this.uppy.on('complete', (result) => {
  //     console.log('Upload complete:', result);
  //   });
  // }

}
