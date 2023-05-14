# simulator
set ns [new Simulator]
set side [lindex $argv 0]
set nnodes [lindex $argv 1]
set nflows [lindex $argv 2]

# ======================================================================
# Define options

set val(chan)         Channel/WirelessChannel  ;# channel type
set val(prop)         Propagation/TwoRayGround ;# radio-propagation model
set val(ant)          Antenna/OmniAntenna      ;# Antenna type
set val(ll)           LL                       ;# Link layer type
set val(ifq)          Queue/DropTail/PriQueue  ;# Interface queue type
set val(ifqlen)       50                       ;# max packet in ifq
set val(netif)        Phy/WirelessPhy/802_15_4     ;# network interface type
set val(mac)          Mac/802_15_4          ;# MAC type
set val(rp)           DSDV                     ;# ad-hoc routing protocol 
set val(nn)           $nnodes                       ;# number of mobilenode_s

set val(nf)         $nflows               ;# number of flows
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


# node_ configs
# ======================================================================

# $ns node_-config -addressingType flat or hierarchical or expanded
#                  -adhocRouting   DSDV or DSR or TORA
#                  -llType	   LL
#                  -macType	   Mac/802_11
#                  -propType	   "Propagation/TwoRayGround"
#                  -ifqType	   "Queue/DropTail/PriQueue"
#                  -ifqLen	   50
#                  -phyType	   "Phy/WirelessPhy"
#                  -antType	   "Antenna/OmniAntenna"
#                  -channelType    "Channel/WirelessChannel"
#                  -topoInstance   $topo
#                  -energyModel    "EnergyModel"
#                  -initialEnergy  (in Joules)
#                  -rxPower        (in W)
#                  -txPower        (in W)
#                  -agentTrace     ON or OFF
#                  -routerTrace    ON or OFF
#                  -macTrace       ON or OFF
#                  -movementTrace  ON or OFF

# ======================================================================

#	TCP Tahoe + Telnet	Random	1 Source, Random Sink

#config with: 802.15.4	DSDV

global defaultRNG
$defaultRNG seed 2022

set val(energymodel_15) EnergyModel ;
set val(initialenergy_15) 300.0 ;# Initial energy in Joules
set val(idlepower_15) 40 ;#LEAP (802.11g)
set val(rxpower_15) 75 ;#LEAP (802.11g)
set val(txpower_15) 75 ;#LEAP (802.11g)
set val(sleeppower_15) 40 ;#LEAP (802.11g)

$ns node-config -adhocRouting $val(rp) \
    -llType $val(ll) \
    -macType $val(mac) \
    -ifqType $val(ifq) \
    -ifqLen $val(ifqlen) \
    -antType $val(ant) \
    -propType $val(prop) \
    -phyType $val(netif) \
    -channel [new $val(chan)] \
    -topoInstance $topo \
    -energyModel $val(energymodel_15) \
    -idlePower $val(idlepower_15) \
    -rxPower $val(rxpower_15) \
    -txPower $val(txpower_15) \
    -sleepPower $val(sleeppower_15) \
    -initialEnergy $val(initialenergy_15) \
    -agentTrace ON \
    -routerTrace OFF \
    -macTrace ON \
    -movementTrace OFF \
    -rx_sens_ -90






set src        [expr int(rand()*$side)% $val(nn)]                 ;# source node_

puts "Source node_: $src"

# set node_($src) [$ns node]
# # $node($src) random-motion 0       ;# disable random motion

# # set node position to random


# $node_($src) set X_ 100
# $node_($src) set Y_ 100
# $node_($src) set Z_ 0

# $ns initial_node_pos $node_($src) 20



# create node_s
for {set i 0} {$i < $val(nn) } {incr i} {

    # if {$i == $src} {
    #     puts "Skipping node_ $i"
    #     continue;
    # }

    set node_($i) [$ns node]
    # $node($i) random-motion 0       ;# disable random motion
    
    # set node position to random



    set x [expr int(rand() * $side)]
    set y [expr int(rand() * $side)]

    # if {$i == $src} {
    #     set x [expr int(0.5 * $side)]
    #     set y [expr int(0.5 * $side)]
    # }
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
    
    set tcp_($i) [new Agent/TCP]
    set tcp_sink_($i) [new Agent/TCPSink]
    # attach to nodes
    $ns attach-agent $node_($src) $tcp_($i)
    $ns attach-agent $node_($dest) $tcp_sink_($i)
    # connect agents
    $ns connect $tcp_($i) $tcp_sink_($i)
    puts "Flow $i: $src -> $dest"
    $tcp_($i) set fid_ $i

    # Traffic generator
    set telnet_($i) [new Application/Telnet]
    $telnet_($i)  set packet_size_ 40
    # $telnet_($i)  set rate 1Mb
    $telnet_($i)  set interval_ 0.1
    # attach to agent
    $telnet_($i)  attach-agent $tcp_($i)
    
    # start traffic generation
    $ns at 0.2 "$telnet_($i)  start"
}

#start random motion with speed  between 1m/s and 5 m/s
for {set i 0} {$i < $val(nn) } {incr i} {
   
    set speed [expr int(rand()*5)+1]
    set x [expr int(rand() * $side)]
    set y [expr int(rand() * $side)]
    if {$x == 0} {set x 1}
    if {$y == 0} {set y 1}
    if {$x == $side} {set x [expr $side - 1]}
    if {$y == $side} {set y [expr $side - 1]}
    $ns at 0.2 "$node_($i) setdest $x $y $speed"
    # $ns at 0.2 "$node_($i) setdest 490.0 480.0 10"
}

# End Simulation

# Stop node_s
for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at 5.0 "$node_($i) reset"
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

$ns at 5.0001 "finish"
$ns at 5.0002 "halt_simulation"



# Run simulation
puts "Simulation starting"
$ns run
