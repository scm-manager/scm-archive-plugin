/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
Sonia.repository.RepositoryBrowser.prototype.createTopToolbarExt = Sonia.repository.RepositoryBrowser.prototype.createTopToolbar;

Ext.override(Sonia.repository.RepositoryBrowser, {
  
  downloadButtonText: 'Download Archive',
  archiveIcon: 'resources/images/archive.png',
  
  createTopToolbar: function(){
    var ttbar = this.createTopToolbarExt();
    if ( ttbar ){
      if ( ttbar.items.indexOf('->') < 0 && ttbar.items[0].indexOf('->') < 0){
        ttbar.items.push('->');
      }
      ttbar.items.push({
        text: this.downloadButtonText,
        icon: this.archiveIcon,
        handler: function(){
          var url = restUrl + "plugins/archive/" + this.repository.id + '.zip';
          if (this.revision){
            url += '?revision=' + this.revision;
          }
          if (this.path){
            if (this.revision){
              url += '&';
            } else {
              url += '?';
            }
            url += 'path=' + this.path;
          }
          window.open( url );
        },
        scope: this
      });
    }
    return ttbar;
  }
  
});