package javaff.scheduling;


public enum MutexType
{
	ControlObject, //originated in AUTOGRAPH, but here for simplicity
	ApauseB,
	BpauseA,
	AdeleteBadd,
	BdeleteAadd,
	AdeleteBpc,
	BdeleteApc,
	AddMutex,
	CompetingPCs,
	None,
}
