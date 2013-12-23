// Original source:
// http://winxblog.com/2009/02/iphone-uiwebview-estimated-progress/

@interface UIWebDocumentView : NSObject
{
	WebView *_webView;
    float _documentScale;
}

- (WebView *)webView;
- (float)initialScale;
- (float)_documentScale;

@end

@interface UIWebView (DocumentView)

- (UIWebDocumentView *)_documentView;

@end