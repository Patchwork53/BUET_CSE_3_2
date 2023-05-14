# simulator
set ns [new Simulator]
set side [lindex $argv 0]
set nnodes [lindex $argv 1]
set nflows [lindex $argv 2]
set packet_per_sec [lindex $argv 3]
set coefficientOfTX   [lindex $argv 4]

# ======================================================================
# Define options

set val(chan)         Channel/WirelessChannel  ;# channel type
set val(prop)         Propagation/TwoRayGround ;# radio-propagation model
set val(ant)          Antenna/OmniAntenna      ;# Antenna type
set val(ll)           LL                       ;# Link layer type
set val(ifq)          Queue/DropTail/PriQueue  ;# Interface queue type
set val(ifqlen)       50                       ;# max packet in ifq
set val(netif)        Phy/WirelessPhy          ;# network interface type
set val(mac)          Mac/802_11               ;# MAC type
set val(rp)           DSDV                     ;# ad-hoc routing protocol 
set val(nn)           $nnodes                  ;# number of mobilenode_s
set val(nf)           $nflows                  ;# number of flows
set val(energymodel_11)    			EnergyModel     ;
set val(initialenergy_11)  			1000            ;
set val(idlepower_11) 				900e-3			; 
set val(rxpower_11) 				925e-3			;
set val(txpower_11) 				1425e-3			;
set val(sleeppower_11) 				300e-3			;
set val(transitionpower_11) 		200e-3			;	
set val(transitiontime_11) 			3				;



set nowValue [Phy/WirelessPhy set Pt_]              ;#   0.001
puts "INSIDE TCL FILE, INITIAL VALUE of Pt_ = $nowValue of Pt_"
set newValue_Pt [expr $coefficientOfTX * $coefficientOfTX * $nowValue];	

puts "INSIDE TCL FILE, NEW VALUE of Pt_ = $newValue_Pt of Pt_"
Phy/WirelessPhy set Pt_ 			$newValue_Pt;	

# =======================================================================

# trace file
set trace_file [open trace.tr w]
$ns trace-all $trace_file

# nam file
set nam_file [open anime.nam w]
$ns namtrace-all-wireless $nam_file $side $side

# topology: to keep track of node_ movements
set topo [new Topography]
$topo load_flatgrid $side $side ;# $sidem x $sidem area


# general operation director for mobilenode_s
create-god $val(nn)

set dropRate 0.1

proc UniformErr {} {
    global dropRate
    set err [new ErrorModel/Uniform $dropRate pkt]
    return $err
}


#config with: 802.11	DSDV     Static One Src Many Sink

global defaultRNG
$defaultRNG seed 2022

$ns node-config -adhocRouting $val(rp) \
    -llType $val(ll) \
    -macType $val(mac) \
    -ifqType $val(ifq) \
    -ifqLen $val(ifqlen) \
    -antType $val(ant) \
    -propType $val(prop) \
    -IncomingErrProc UniformErr \
    -phyType $val(netif) \
    -channel [new $val(chan)] \
    -topoInstance $topo \
    -agentTrace ON \
    -routerTrace OFF \
    -macTrace ON \
    -movementTrace OFF \
    -energyModel $val(energymodel_11) \
    -idlePower $val(idlepower_11) \
    -rxPower $val(rxpower_11) \
    -txPower $val(txpower_11) \
    -sleepPower $val(sleeppower_11) \
    -transitionPower $val(transitionpower_11) \
    -transitionTime $val(transitiontime_11) \
    -initialEnergy $val(initialenergy_11)






set src        [expr int(rand()*$side)% $val(nn)]                 ;# source node_

puts "Source node_: $src"

# create node_s
for {set i 0} {$i < $val(nn) } {incr i} {

    set node_($i) [$ns node]


    set x [expr int(rand() * $side)]
    set y [expr int(rand() * $side)]

    $node_($i) set X_ $x
    $node_($i) set Y_ $y
    $node_($i) set Z_ 0

    $ns initial_node_pos $node_($i) 20
} 


# Traffic

for {set i 0} {$i < $val(nf)} {incr i} {
    

    set dest [expr int(rand()*$side)% $val(nn)]                 ;# destination node

    if {$dest == $src} {
        puts "Skipping flow $dest"
        incr i -1;
        continue;
    }
    
    set tcp_($i) [new Agent/TCP/Linux]
   
    $tcp_($i) set timestamps_ true
    set tcp_sink_($i) [new Agent/TCPSink/Sack1]

    # attach to nodes
    $ns attach-agent $node_($src) $tcp_($i)
    $ns attach-agent $node_($dest) $tcp_sink_($i)

    # connect agents
    $ns connect $tcp_($i) $tcp_sink_($i)
    puts "Flow $i: $src -> $dest"
    $tcp_($i) set fid_ $i

    # Traffic generator
    set ftp_($i) [new Application/FTP]
    $ftp_($i)  set packet_size_ 1024
 
    $ftp_($i)  set interval_ [expr 1.0/$packet_per_sec]
  
    $ftp_($i)  attach-agent $tcp_($i)
    
    # start traffic generation

    $ns at 0 "$tcp_($i) select_ca cubic"
    $ns at 0.2 "$ftp_($i)  start"
}


# End Simulation

set time 20.0
# Stop node_s
for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at $time "$node_($i) reset"
}

# call final function
proc finish {} {
    global ns trace_file nam_file
    $ns flush-trace
    close $trace_file
    close $nam_file
}

proc halt_simulation {} {
    global ns
    puts "Simulation ending"
    $ns halt
}


$ns at [expr $time+0.001] "finish"
$ns at [expr $time+0.002] "halt_simulation"



# Run simulation
puts "Simulation starting"
$ns run
