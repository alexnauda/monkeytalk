Vars * define usr=fred pw=secret123
TabBar * SelectIndex 1
Input username Tap
Input username Tap 2
Input username EnterText ${usr}
Input password EnterText ${pw} enter
Device * Screenshot
Button LOGIN Tap
Label * verify "Welcome, ${usr}!" %timeout=20000
Button LOGOUT Tap %timeout=20000
Device * Screenshot
