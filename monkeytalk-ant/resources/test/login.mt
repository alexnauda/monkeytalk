Vars * Define usr pwd
Input username EnterText ${usr}
Input password EnterText ${pwd} 
Button LOGIN Tap
Script logout.mt Run ${usr}
