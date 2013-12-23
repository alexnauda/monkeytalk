package
{
	import flash.events.Event;
	import flash.events.HTTPStatusEvent;
	import flash.events.IOErrorEvent;
	import flash.events.SecurityErrorEvent;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	
	import mx.utils.ObjectUtil;
	
	public class LoadQueuer extends Object
	{
		// Queue
		private var _queue:Array = [];
		public var updateMessageLog:Function;
		/**
		 * Standard load
		 * @param req Instance of URLRequest
		 */
		public function queue(loader:URLLoader, request:URLRequest):void
		{
			_queue.push({LOADER: loader, REQUEST: request});
			_next();
		}
		
		/**
		 * Loads next loader in _queue
		 */
		private function _next():void
		{
			if(_queue.length > 0)
			{
				var info:Object = _queue.pop();
				
				var loader:URLLoader = URLLoader(info.LOADER);
				
				loader.load(
					URLRequest(info.REQUEST)
				);
				
				loader.addEventListener(Event.COMPLETE, _complete);
				// Only supported by some browsers
				//loader.addEventListener(HTTPStatusEvent.HTTP_STATUS, );
				loader.addEventListener(Event.OPEN, function(e:Event):void{
					updateMessageLog("OPEN\n");
					updateMessageLog(mx.utils.ObjectUtil.toString(e));
				});
				
				
				// AIR only
				loader.addEventListener(HTTPStatusEvent.HTTP_RESPONSE_STATUS, function(e:Event):void{
					updateMessageLog("RESP CODE: " + e["status"] + "\n");
				});
				
				
				loader.addEventListener(SecurityErrorEvent.SECURITY_ERROR, function(e:Event):void{
					updateMessageLog(mx.utils.ObjectUtil.toString(e));
				});
				loader.addEventListener(IOErrorEvent.IO_ERROR, function(e:Event):void{
					var io:IOErrorEvent = e as IOErrorEvent;
					updateMessageLog(io.text + "\n");
					var loader2:URLLoader = e.target as URLLoader;
					
					updateMessageLog(mx.utils.ObjectUtil.toString(loader2));
				});   
			}
		}
		
		/**
		 * ..
		 * Event.COMPLETE
		 */
		private function _complete(e:Event):void
		{
			var loader:URLLoader = URLLoader(e.target);
			loader.removeEventListener(Event.COMPLETE, _complete);
			var responseJson : String = loader.data as String;
			updateMessageLog("RESP:" + responseJson + "\n");
			
			_next();
		}
	}
}
