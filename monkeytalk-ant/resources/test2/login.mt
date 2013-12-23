Vars * Define usr=Héìíô⇐⇑⇝⇜
Input username EnterText ${usr}
Input password EnterText password
Button LOGIN Tap
Button LOGOUT Verify %timeout=3000
Label * Verify "Welcome, ${usr}!"
Button LOGOUT Tap %thinktime=1000
