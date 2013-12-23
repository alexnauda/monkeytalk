-------------------------------------------------------------------------------
MonkeyTalk Agent - Flex
-------------------------------------------------------------------------------

These are the MonkeyTalk Agents for Flex.

1. CONTENTS

This folder contains the four different agents for Adobe Flex.

1) automation_monkey4.x.swc
2) automation_monkey3.x.swc
3) automation_monkey3.3.swc
4) automation_monkey3.2.swc

The agent you choose depends on your version of Adobe Flex.


2. INSTALLATION

Installing the MonkeyTalk agent in your Flex app is a two step process:

Step #1 - copy the agent into the libs folder (choose the correct agent .swc
from the above list depending on your version of Flex).

Step #2 - add the agent, plus the Adobe Flash Builder Pro automation libraries,
to your Flex app's compiler options in the "additional compiler arguments" box.

Do this: right-click > Project Properties > Flex Compiler

Then:
for Flex SDK 4 or greater the additional compiler arguments are:
-include-libraries "../libs/automation_monkey4.x.swc" "${flexlib}/libs/automation/automation_spark.swc" "${flexlib}/libs/automation/automation.swc" "${flexlib}/libs/automation/automation_agent.swc" "${flexlib}/libs/automation/automation_dmv.swc"

for an Adobe AIR app with Flex SDK 4 or greater the additional compiler arguments are:
-include-libraries "../libs/automation_monkey4.x.swc" "${flexlib}/libs/automation/automation_spark.swc" "${flexlib}/libs/automation/automation.swc" "${flexlib}/libs/automation/automation_agent.swc" "${flexlib}/libs/automation/automation_dmv.swc" "${flexlib}/libs/automation/automation_air.swc" "${flexlib}/libs/automation/automation_airspark.swc"

for Flex SDK 3.5 up to 4.0 the additional compiler arguments are:
-include-libraries "../libs/automation_monkey3.x.swc" "${flexlib}/libs/automation.swc" "${flexlib}/libs/automation_agent.swc" "${flexlib}/libs/automation_dmv.swc"

for Flex SDK 3.3 the additional compiler arguments are:
-include-libraries "../libs/automation_monkey3.3.swc" "${flexlib}/libs/automation.swc" "${flexlib}/libs/automation_agent.swc" "${flexlib}/libs/automation_dmv.swc"

for Flex SDK 3.2 the additional compiler arguments are:
-include-libraries "../libs/automation_monkey3.2.swc" "${flexlib}/libs/automation.swc" "${flexlib}/libs/automation_agent.swc" "${flexlib}/libs/automation_dmv.swc"

Just copy-and-paste these args into the box in addition to any args that may
already be there.


3. DOCUMENTATION

Find more information about MonkeyTalk in general, and specifically the
MonkeyTalk Agent for Flex online, including screenshots for the complete
installation process.

MonkeyTalk:
http://www.gorillalogic.com/monkeytalk

MonkeyTalk Documentation:
http://www.gorillalogic.com/monkeytalk/docs

MonkeyTalk Agent for Flex installation:
http://www.gorillalogic.com/monkeytalk/docs/install_flex_agent
