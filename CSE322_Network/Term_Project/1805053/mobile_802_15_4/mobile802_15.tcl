# simulator
set ns [new Simulator]
set side [lindex $argv 0]
set nnodes [lindex $argv 1]
set nflows [lindex $argv 2]
set packet_per_sec [lindex $argv 3]
set speed   [lindex $argv 4]

# ======================================================================
# Define options

set val(chan)         Channel/WirelessChannel  ;# channel type
set val(prop)         Propagation/TwoRayGround ;# radio-propagation model
set val(ant)          Antenna/OmniAntenna      ;# Antenna type
set val(ll)           LL                       ;# Link layer type
set val(ifq)          Queue/DropTail/PriQueue  ;# Interface queue type
set val(ifqlen)       50                       ;# max packet in ifq
set val(netif)        Phy/WirelessPhy/802_15_4 ;# network interface type
set val(mac)          Mac/802_15_4             ;# MAC type
set val(rp)           DSDV                     ;# ad-hoc routing protocol 
set val(nn)           $nnodes                  ;# number of mobilenode_s

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


set dropRate 0.1

proc UniformErr {} {
    global dropRate
    set err [new ErrorModel/Uniform $dropRate pkt]
    return $err
}

global defaultRNG
$defaultRNG seed 2022

set val(energymodel_15) EnergyModel ;
set val(initialenergy_15) 300.0 ;
set val(idlepower_15) 40 ;
set val(rxpower_15) 75 ;
set val(txpower_15) 75 ;
set val(sleeppower_15) 40 ;

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
    # $tcp_($i) set maxseq_ 1000

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
    $ftp_($i)  set packet_size_ 4096
    $ftp_($i)  set interval_ [expr 1.0/$packet_per_sec]
  
    $ftp_($i)  attach-agent $tcp_($i)
    
    # start traffic generation
    $ns at 0 "$tcp_($i) select_ca cubic"
    $ns at 0.2 "$ftp_($i)  start"
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

}

# End Simulation

set time 1000.0
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